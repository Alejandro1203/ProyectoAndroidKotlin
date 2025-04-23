package com.example.proyectoandroidkotlin.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.databinding.RegistroLayoutBinding
import com.example.proyectoandroidkotlin.entidades.GrupoUsuarioEntidad
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.GrupoUsuarioBBDD
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@SuppressLint("SimpleDateFormat")
class RegistroActivity: AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_DIALOG_CAMARA = 2000
        private const val REQUEST_CODE_DIALOG_GALERIA = 3000
        private const val REQUEST_CODE_PERMISO_LOCALIZACION = 100
        private const val REQUEST_CODE_PERMISO_CAMARA = 200
        private const val REQUEST_CODE_PERMISO_GALERIA = 300
        private val formatoUltimaModificacion = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        private val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"))
    }

    private lateinit var binding: RegistroLayoutBinding
    private val usuarioBBDD by lazy { UsuarioBBDD(this) }
    private val grupoUsuarioBBDD by lazy { GrupoUsuarioBBDD(this) }
    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var bundleRecogida: Bundle ?= null
    private var listaGrupoUsuario: List<GrupoUsuarioEntidad> = emptyList()
    private var fechaSeleccionada: Long = -1L
//    lateinit var dialogCamara: MaterialAlertDialogBuilder
    private var foto: Bitmap ?= null
    private var uriFoto: Uri ?= null
    private var fotoRutaAvatar: String = ""
//    private var fotoRutaGaleria: String = ""
    private var nombre: String = ""
    private var correo: String = ""
    private var fechaNacimiento: String = ""
    private var contrasenya: String = ""
    private var rol: String = ""
    private var idRol: Int = -1
    private var baja: Int = 0
    private var ultimaModificacion: String = ""
    private var latitud: String = "0.0"
    private var longitud: String = "0.0"
    private var usuarioEditar: UsuarioEntidad ?= null
    private var usuarioEditor: UsuarioEntidad ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegistroLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rellenarSpinnerRoles()

        bundleRecogida = intent.extras

        if(bundleRecogida != null) {
            usuarioEditor = bundleRecogida?.getSerializable("usuarioEditor") as UsuarioEntidad
            usuarioEditar = bundleRecogida?.getSerializable("usuarioEditar") as UsuarioEntidad

            setVistaEdicion()

            binding.iconoUsuario.setOnClickListener {  }

            binding.btnMapa.setOnClickListener {
                abrirMapa()
            }

            binding.btnFoto.setOnClickListener {
                crearDialogOpcionesImagenes()
            }

            binding.btnRegistrar.setOnClickListener {  }

        } else {
            binding.btnFoto.setOnClickListener { crearDialogCamara(REQUEST_CODE_DIALOG_CAMARA) }

            binding.btnRegistrar.setOnClickListener {
                if(camposRellenos()) {
                    nombre = binding.edtNombre.editText?.text.toString().trim()
                    correo = binding.edtCorreo.editText?.text.toString().trim()
                    fechaNacimiento = binding.datePicker.editText?.text.toString().trim()

                    if(validarCampos(nombre, correo, fechaNacimiento)) {
                        if(usuarioBBDD.correoExiste(correo)) {
                            Snackbar.make(binding.snackbar, R.string.error_correo, Snackbar.LENGTH_SHORT).show()
                        } else {
                            contrasenya = binding.edtContrasenya.editText?.text.toString().trim()
                            ultimaModificacion = formatoUltimaModificacion.format(calendar.time)

                            if(getLocalizacion()) {
                                localizacionConseguida()
                            }
                        }
                    }
                } else {
                    Snackbar.make(binding.snackbar, R.string.error_campos_incompletos, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.txtIniciado.setOnClickListener { v ->
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.datePicker.setEndIconOnClickListener { v ->
            val calendarBuilder = CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.seleccionar_fecha)
                .setSelection(if(fechaSeleccionada != -1L) {
                                  fechaSeleccionada
                              } else {
                                  MaterialDatePicker.todayInUtcMilliseconds()
                              })
                .setCalendarConstraints(calendarBuilder.build())
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                fechaSeleccionada = selection
                fechaNacimiento = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(selection))

                binding.datePicker.editText?.setText(fechaNacimiento)
            }

            picker.show(supportFragmentManager, "tag")
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                rol = listaGrupoUsuario[position].rol
                idRol = listaGrupoUsuario[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private val launcherAvatarCamara: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode == RESULT_OK && result.data != null) {
            foto = result.data?.extras?.get("data") as? Bitmap
            binding.iconoUsuario.setImageBitmap(foto)
            binding.iconoUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP)
            uriFoto = getUriFoto(applicationContext, foto)
            if (getRutaFromUri(uriFoto) != null) {
                fotoRutaAvatar = getRutaFromUri(uriFoto)!!
            }
        }
    }

    private val launcherAvatarGaleria: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode == RESULT_OK && result.data != null && result.data!!.data != null ) {
            uriFoto = result.data!!.data

            uriFoto?.let { imageDecoder(it) }

            if(foto != null) {
                if (getRutaFromUri(uriFoto) != null) {
                    fotoRutaAvatar = getRutaFromUri(uriFoto)!!
                    binding.iconoUsuario.setImageBitmap(BitmapFactory.decodeFile(fotoRutaAvatar))
                    binding.iconoUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP)
                }

            } else {
                Toast.makeText(this, R.string.error_cargando_imagen, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val launcherGaleriaCamara: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

    }

    private val launcherGaleriaGaleria: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

    }


    private fun camposRellenos(): Boolean {
        return binding.edtNombre.editText?.text.toString().trim().isNotEmpty() &&
               binding.edtCorreo.editText?.text.toString().trim().isNotEmpty() &&
               binding.edtContrasenya.editText?.text.toString().trim().isNotEmpty() &&
               binding.datePicker.editText?.text.toString().trim().isNotEmpty() &&
               fotoRutaAvatar != null
    }

    private fun validarNombre(nombre: String): Boolean {
        if(nombre.length <= 20) {
            return true
        } else {
            Snackbar.make(binding.snackbar, R.string.error_longitud_nombre, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    private fun validarCorreo(correo: String): Boolean {
        if(Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return true
        } else {
            Snackbar.make(binding.snackbar, R.string.error_formato_correo, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    private fun validarFechaNacimiento(fecha: String): Boolean {

        try {
            LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            return true
        } catch (e: DateTimeParseException) {
            Snackbar.make(binding.snackbar, R.string.error_formato_fecha, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    private fun validarCampos(nombre: String, correo: String, fecha: String): Boolean {
        return validarNombre(nombre) && validarCorreo(correo) && validarFechaNacimiento(fecha)
    }

    private fun limpiarCampos() {
        binding.edtNombre.editText?.setText("")
        binding.edtCorreo.editText?.setText("")
        binding.edtContrasenya.editText?.setText("")
        binding.datePicker.editText?.setText("")
        binding.iconoUsuario.setImageResource(R.drawable.usuario)
    }

    private fun rellenarSpinnerRoles() {
        listaGrupoUsuario = grupoUsuarioBBDD.getAllGrupoUsuarios()
        var roles = arrayListOf<String>()

        listaGrupoUsuario.forEach {
            roles.add(it.rol)
        }

        var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinner.adapter = adaptador
        binding.spinner.setSelection(1)
    }

    private fun imageDecoder(uri: Uri) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var source = ImageDecoder.createSource(this.contentResolver, uri)
                foto = ImageDecoder.decodeBitmap(source)
            } else {
                var inputStream = contentResolver.openInputStream(uri)

                if(inputStream != null) {
                    foto = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, R.string.error_cargando_imagen, Toast.LENGTH_SHORT).show()
            Log.e(getString(R.string.error_decodificar_imagen), getString(R.string.metodo_imageDecoder) + ":$e")
        }
    }

    private fun getUriFoto(context: Context, image: Bitmap?): Uri? {
        var values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "imagen_" + System.currentTimeMillis() + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        var imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            if (image != null) {
                imageUri?.let { context.contentResolver.openOutputStream(it) }.use { outputStream ->
                    image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
                }
            }
        } catch (e: IOException) {
            Log.e(getString(R.string.error_uri), getString(R.string.metodo_getUriFoto) + ":$e")
        }

        return imageUri
    }

    private fun getRutaFromUri(uri: Uri?): String? {
        var nombreArchivo = "imagen_" + System.currentTimeMillis() + ".jpg"
        var directorioDestino = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "imagenes")

        if(!directorioDestino.exists()) {
            directorioDestino.mkdirs()
        }

        var archivoDestino = File(directorioDestino, nombreArchivo)

        try {
            uri?.let { contentResolver.openInputStream(it) }.use { inputStream ->
                FileOutputStream(archivoDestino).use { outputStream ->
                    var buffer = ByteArray(4096)
                    var bytesRead: Int = -1

                    while ({bytesRead = inputStream?.read(buffer)!!; bytesRead }() != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    return archivoDestino.absolutePath
                }
            }
        } catch (e: IOException) {
            Log.e(getString(R.string.error_ruta_imagen),  getString(R.string.metodo_getRutaFromUri) + ":$e")
            return null
        }
    }

    private fun setVistaEdicion() {
        binding.txtUltimaModificacion.visibility = VISIBLE
        binding.btnMapa.visibility = VISIBLE
        binding.txtIniciado.visibility = GONE

        if(usuarioEditor?.rol == 2 || (usuarioEditor == usuarioEditar)) {
            binding.spinner.visibility = GONE
            binding.switchBaja.visibility = GONE
        } else if(usuarioEditor?.rol == 1) {
            binding.switchBaja.visibility = VISIBLE
            binding.switchBaja.isChecked = usuarioEditar?.baja == 1
        }

        binding.iconoUsuario.setImageURI(usuarioEditar?.fotoPerfil?.toUri())
        binding.txtUltimaModificacion.text = usuarioEditar?.ultimaModificacion
        binding.edtNombre.editText?.setText(usuarioEditar?.nombre)
        binding.edtCorreo.editText?.setText(usuarioEditar?.correo)
        binding.edtContrasenya.editText?.setText(usuarioEditar?.contrasenya)
        binding.datePicker.editText?.setText(usuarioEditar?.fechaNacimiento)
        usuarioEditar?.rol?.let { binding.spinner.setSelection(it - 1) }
        binding.btnRegistrar.text = getString(R.string.btn_actualizar)
    }

    private fun crearDialogCamara(requestCode: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.elegir_foto)
            .setNeutralButton(R.string.dialog_cancelar, null)
            .setPositiveButton("") { dialog, which ->
                if(tienePermisoCamara()) {
                    abrirCamara(requestCode)
                } else {
                    pedirPermisoCamara()
                }
            }
            .setNegativeButton("") { dialog, which ->
                if(tienePermidoGaleria()) {
                    abrirGaleria(requestCode)
                } else {
                    pedirPermisoGaleria()
                }
            }
            .setNegativeButtonIcon(ContextCompat.getDrawable(this, R.drawable.galeria))
            .setPositiveButtonIcon(ContextCompat.getDrawable(this, R.drawable.camara))
            .show()
    }

    private fun crearDialogOpcionesImagenes() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.messageDialogUsuario)
            .setNeutralButton(R.string.dialog_cancelar, null)
            .setPositiveButton(R.string.insertar_galeria) { dialog, which ->
                crearDialogCamara(REQUEST_CODE_DIALOG_GALERIA)
            }
            .setNegativeButton(R.string.cambiar_foto) { dialog, which ->
                crearDialogCamara(REQUEST_CODE_DIALOG_CAMARA)
            }
            .show()
    }

    private fun abrirMapa() {
        val intentUri = "google.streetview:cbll=${usuarioEditar?.latitud},${usuarioEditar?.longitud}".toUri()
        val intentMapa = Intent(Intent.ACTION_VIEW, intentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if(intentMapa.resolveActivity(packageManager) != null) {
            startActivity(intentMapa)
        } else {
            val webUri = "https://www.google.com/maps/@${usuarioEditar?.latitud},${usuarioEditar?.longitud},15z".toUri()
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }

    private fun getLocalizacion(): Boolean {
        var localizacionConseguida = false

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if(location != null) {
                        latitud = location.latitude.toString()
                        longitud = location.longitude.toString()
                    }
                }
            localizacionConseguida = true
        } else {
            pedirPermisoLocalizacion()
        }

        return localizacionConseguida
    }

    private fun localizacionConseguida() {
        binding.btnRegistrar.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed( {
            val usuario = UsuarioEntidad(nombre, correo, contrasenya, fechaNacimiento, idRol, fotoRutaAvatar, baja, "", ultimaModificacion, latitud, longitud)

            if(usuarioBBDD.insertarUsuario(usuario)) {
                startActivity(Intent(this, LoginActivity::class.java))
                limpiarCampos()
                finish()
            } else {
                binding.btnRegistrar.isEnabled = true
            }
        }, 2000)
    }

    private fun abrirCamara(requestCode: Int) {
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(requestCode == REQUEST_CODE_DIALOG_CAMARA) {
            launcherAvatarCamara.launch(intent)
        } else if(requestCode == REQUEST_CODE_PERMISO_GALERIA){
            launcherGaleriaCamara.launch(intent)
        }

    }

    private fun abrirGaleria(requestCode: Int) {
        intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI).apply {
            setType("image/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }

        if(requestCode == REQUEST_CODE_DIALOG_CAMARA) {
            launcherAvatarGaleria.launch(intent)
        } else if(requestCode == REQUEST_CODE_PERMISO_GALERIA) {
            launcherGaleriaGaleria.launch(intent)
        }

    }

    private fun pedirPermisoLocalizacion() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_PERMISO_LOCALIZACION)
    }

    private fun pedirPermisoCamara() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISO_CAMARA)
    }

    private fun pedirPermisoGaleria() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISO_GALERIA)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISO_GALERIA)
        }
    }

    private fun tienePermisoCamara(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun tienePermidoGaleria(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == REQUEST_CODE_PERMISO_LOCALIZACION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(getLocalizacion()) {
                    localizacionConseguida()
                }

            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_permiso_requerido)
                    .setMessage(R.string.dialog_aceptar_permiso)
                    .setNegativeButton(R.string.dialog_ajustes) { dialog, which ->
                        intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        var uri = Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .show()
            }
        } else if(requestCode == REQUEST_CODE_PERMISO_CAMARA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara(REQUEST_CODE_DIALOG_CAMARA)
        } else if(requestCode == REQUEST_CODE_PERMISO_GALERIA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria(REQUEST_CODE_DIALOG_GALERIA)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}