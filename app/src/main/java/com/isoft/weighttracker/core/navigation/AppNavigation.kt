package com.isoft.weighttracker.core.navigation

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.isoft.weighttracker.feature.persona.PersonaHomeScreen
import com.isoft.weighttracker.feature.login.ui.LoginScreen
import com.isoft.weighttracker.feature.login.viewmodel.LoginViewModel
import com.isoft.weighttracker.feature.comidas.viewmodel.ComidaViewModel
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.feature.selectRole.ui.SelectRoleScreen
import com.isoft.weighttracker.feature.DatosPersonales.ui.DatosPersonalesScreen
import com.isoft.weighttracker.feature.comidas.ui.RegistrarComidasScreen
import com.isoft.weighttracker.feature.comidas.ui.HistorialComidasScreen
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.ui.HistorialActividadFisicaScreen
import com.isoft.weighttracker.feature.actividadfisica.ui.RegistrarActividadFisicaScreen
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import com.isoft.weighttracker.feature.asociar.ui.AsociarProfesionalScreen
import com.isoft.weighttracker.feature.antropometria.ui.HistorialAntropometricoScreen
import com.isoft.weighttracker.feature.antropometria.ui.RegistroAntropometricoScreen
import com.isoft.weighttracker.feature.metas.ui.HistorialMetasScreen
import com.isoft.weighttracker.feature.metas.ui.RegistrarMetaScreen
import com.isoft.weighttracker.feature.comidas.model.Comida
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.core.permissions.PermissionViewModel
import com.isoft.weighttracker.feature.profesional.planes.ui.PlanesScreen
import com.isoft.weighttracker.feature.profesional.reportes.ReportesScreen
import com.isoft.weighttracker.feature.profesional.ui.DatosProfesionalScreen
import com.isoft.weighttracker.feature.profesional.ui.ProfesionalHomeScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.DetalleReporteScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.HistorialReportesScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.RegistrarReporteScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    requestPermission: (String) -> Unit = {},
    permissionViewModel: PermissionViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val comidaViewModel: ComidaViewModel = viewModel()
    val antropometriaViewModel: AntropometriaViewModel = viewModel()
    val actividadFisicaViewModel: ActividadFisicaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }

        composable("selectRole") {
            val viewModel: LoginViewModel = viewModel()
            SelectRoleScreen { role ->
                viewModel.updateUserRole(role)
                navController.navigate("home/$role") {
                    popUpTo("selectRole") { inclusive = true }
                }
            }
        }

        composable("home/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            when (role) {
                "persona" -> PersonaHomeScreen(navController)
                "entrenador", "nutricionista" -> ProfesionalHomeScreen(navController, role)
                else -> Text("Rol no reconocido")
            }
        }

        composable("datosPersonales") {
            DatosPersonalesScreen(navController)
        }

        composable("asociarProfesional") {
            AsociarProfesionalScreen(navController)
        }

        composable("historialReporte") {
            HistorialReportesScreen(navController)
        }

        composable("registrarReporte") {
            RegistrarReporteScreen(navController)
        }

        composable("detalleReporte/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetalleReporteScreen(navController, reporteId = id)
        }

        composable("historialComidas") {
            HistorialComidasScreen(navController, comidaViewModel)
        }

        composable(
            "registrarComida?comida={comida}",
            arguments = listOf(navArgument("comida") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("comida")
            val decoded = json?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
            val comida = decoded?.let { Gson().fromJson(it, Comida::class.java) }
            RegistrarComidasScreen(navController, comida, comidaViewModel)
        }

        composable("historialActividad") {
            HistorialActividadFisicaScreen(
                navController = navController,
                viewModel = actividadFisicaViewModel,
                permissionViewModel = permissionViewModel,
                requestPermission = requestPermission
            )
        }

        composable(
            "registrarActividad?actividad={actividad}",
            arguments = listOf(navArgument("actividad") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("actividad")
            val decoded = json?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
            val actividad = decoded?.let { Gson().fromJson(it, ActividadFisica::class.java) }

            RegistrarActividadFisicaScreen(navController, actividad, actividadFisicaViewModel)
        }

        composable("historialAntropometrico") {
            HistorialAntropometricoScreen(
                navController = navController,
                onNuevoRegistro = { registro ->
                    val json = URLEncoder.encode(Gson().toJson(registro), StandardCharsets.UTF_8.toString())
                    navController.navigate("registroAntropometrico?registro=$json")
                },
                permissionViewModel = permissionViewModel,
                requestPermission = requestPermission
            )
        }

        composable(
            "registroAntropometrico?registro={registro}",
            arguments = listOf(navArgument("registro") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("registro")
            val decoded = json?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
            val registro = decoded?.let { Gson().fromJson(it, Antropometria::class.java) }

            RegistroAntropometricoScreen(
                navController = navController,
                registroExistente = registro
            )
        }

        composable("historialMetas") {
            HistorialMetasScreen(navController)
        }
        composable("registrarMeta") {
            RegistrarMetaScreen(navController)
        }

        //Profesionales
        composable("datosProfesional") {
            DatosProfesionalScreen(navController)
        }

        composable("planesSolicitados") {
            PlanesScreen(navController)
        }

        composable("reportesClientes") {
            ReportesScreen(navController)
        }

    }
}