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
import com.isoft.weighttracker.core.permissions.PermissionViewModel
import com.isoft.weighttracker.feature.DatosPersonales.ui.DatosPersonalesScreen
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.ui.HistorialActividadFisicaScreen
import com.isoft.weighttracker.feature.actividadfisica.ui.RegistrarActividadFisicaScreen
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.antropometria.ui.HistorialAntropometricoScreen
import com.isoft.weighttracker.feature.antropometria.ui.RegistroAntropometricoScreen
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.feature.asociar.ui.AsociarProfesionalScreen
import com.isoft.weighttracker.feature.comidas.model.Comida
import com.isoft.weighttracker.feature.comidas.ui.HistorialComidasScreen
import com.isoft.weighttracker.feature.comidas.ui.RegistrarComidasScreen
import com.isoft.weighttracker.feature.comidas.viewmodel.ComidaViewModel
import com.isoft.weighttracker.feature.login.ui.LoginScreen
import com.isoft.weighttracker.feature.login.viewmodel.LoginViewModel
import com.isoft.weighttracker.feature.metas.ui.HistorialMetasScreen
import com.isoft.weighttracker.feature.metas.ui.RegistrarMetaScreen
import com.isoft.weighttracker.feature.persona.PersonaHomeScreen
import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.feature.planes.ui.profesional.CrearPlanEntrenamientoScreen
import com.isoft.weighttracker.feature.planes.ui.persona.MisPlanesScreen
import com.isoft.weighttracker.feature.planes.ui.profesional.PlanesCreadosUsuariosScreen
import com.isoft.weighttracker.feature.planes.ui.profesional.PlanesUsuarioScreen
import com.isoft.weighttracker.feature.planes.ui.persona.SolicitarPlanScreen
import com.isoft.weighttracker.feature.planes.ui.profesional.SolicitudesProfesionalScreen
import com.isoft.weighttracker.feature.planes.ui.persona.VerPlanEntrenamientoScreen
import com.isoft.weighttracker.feature.profesional.datosProf.ui.DatosProfesionalScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.profesional.ReportesScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.profesional.RetroalimentacionScreen
import com.isoft.weighttracker.feature.profesional.ui.ProfesionalHomeScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.persona.DetalleReporteScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.persona.GraficasAnaliticasScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.persona.HistorialReportesScreen
import com.isoft.weighttracker.feature.reporteAvance.ui.persona.RegistrarReporteScreen
import com.isoft.weighttracker.feature.selectRole.ui.SelectRoleScreen
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

        // === RUTAS DE REPORTES DE AVANCE (USUARIO) ===
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

        composable("graficasAnaliticas") {
            GraficasAnaliticasScreen(navController)
        }

        // === RUTAS DE COMIDAS ===
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

        // === RUTAS DE ACTIVIDAD FÍSICA ===
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

        // === RUTAS DE ANTROPOMETRÍA ===
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

        // === RUTAS DE METAS ===
        composable("historialMetas") {
            HistorialMetasScreen(navController)
        }

        composable("registrarMeta") {
            RegistrarMetaScreen(navController)
        }

        // === RUTAS PROFESIONALES ===
        composable("datosProfesional") {
            DatosProfesionalScreen(navController)
        }

        // RUTA CORREGIDA: reporteAvance con parámetro role
        composable("reporteAvance/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            ReportesScreen(navController, role)
        }

        // === RUTA: RETROALIMENTACIÓN ===
        composable(
            "retroalimentacion/{reporteId}/{usuarioId}",
            arguments = listOf(
                navArgument("reporteId") { type = NavType.StringType },
                navArgument("usuarioId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            RetroalimentacionScreen(
                navController = navController,
                reporteId = reporteId,
                usuarioId = usuarioId
            )
        }

        // === ✅ RUTAS DE PLANES (USUARIOS) ===
        composable("solicitarPlan") {
            SolicitarPlanScreen(navController)
        }

        composable("misPlanes") {
            MisPlanesScreen(navController)
        }

        // === ✅ RUTAS DE PLANES (PROFESIONALES) ===
        composable("solicitudesPlanes/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            SolicitudesProfesionalScreen(navController, role)
        }

        composable(
            "crearPlanEntrenamiento/{solicitudJson}",
            arguments = listOf(navArgument("solicitudJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val solicitudJson = backStackEntry.arguments?.getString("solicitudJson") ?: ""
            val decoded = URLDecoder.decode(solicitudJson, StandardCharsets.UTF_8.toString())
            val solicitud = Gson().fromJson(decoded, SolicitudPlan::class.java)
            CrearPlanEntrenamientoScreen(navController, solicitud)
        }

        composable(
            "verPlanEntrenamiento/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            VerPlanEntrenamientoScreen(navController, planId)
        }

        composable(
            "planesCreados/{role}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            PlanesCreadosUsuariosScreen(navController = navController, role = role)
        }

        composable(
            route = "planesUsuario/{userId}/{userName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = URLDecoder.decode(
                backStackEntry.arguments?.getString("userName") ?: "",
                StandardCharsets.UTF_8.toString()
            )

            PlanesUsuarioScreen(
                navController = navController,
                userId = userId,
                userName = userName,
                //Implementado para Entrenador por ahora.
                rol = "entrenador" // puede pasarse a dinámico
            )
        }
    }
}