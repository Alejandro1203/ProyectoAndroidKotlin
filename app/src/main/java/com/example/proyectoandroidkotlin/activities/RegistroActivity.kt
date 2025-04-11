package com.example.proyectoandroidkotlin.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.databinding.RegistroLayoutBinding
import com.example.proyectoandroidkotlin.entidades.EntidadGrupoUsuario
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import com.example.proyectoandroidkotlin.tablasBBDD.GrupoUsuarioBBDD
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.google.android.gms.location.FusedLocationProviderClient
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
    private val REQUEST_CODE_PERMISO_LOCALIZACION = 100
    private val REQUEST_CODE_PERMISO_CAMARA = 200
    private val REQUEST_CODE_PERMISO_GALERIA = 300
    lateinit var usuario: EntidadUsuario
    lateinit var usuarioBBDD: UsuarioBBDD
    lateinit var grupoUsuarioBBDD: GrupoUsuarioBBDD
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var listaGrupoUsuario: List<EntidadGrupoUsuario>
    val formatoUltimaModificacion = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"))
    lateinit var calendarBuilder: CalendarConstraints.Builder
    lateinit var picker: MaterialDatePicker<Long>
    var fechaSeleccionada: Long = -1L
    lateinit var binding: RegistroLayoutBinding
    lateinit var dialogCamara: MaterialAlertDialogBuilder
    lateinit var foto: Bitmap
    lateinit var uriFoto: Uri
    lateinit var fotoRutaAvatar: String
    lateinit var fotoRutaGaleria: String
    lateinit var nombre: String
    lateinit var correo: String
    lateinit var fechaNacimiento: String
    lateinit var contrasenya: String
    lateinit var rol: String
    var idRol: Int = -1
    var baja: Int = 0
    lateinit var ultimaModificacion: String
    lateinit var latitud: String
    lateinit var longitud: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegistroLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        usuarioBBDD = UsuarioBBDD(this)
        grupoUsuarioBBDD = GrupoUsuarioBBDD(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        rellenarSpinnerRoles()

        binding.txtIniciado.setOnClickListener { v ->
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.datePicker.setEndIconOnClickListener { v ->
            calendarBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

            picker = MaterialDatePicker.Builder.datePicker()
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

        binding.btnFoto.setOnClickListener { v ->
            crearDialogCamara()
        }

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

    private val launcherAvatarCamara: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode == RESULT_OK && result.data != null) {
            foto = result.data?.extras?.get("data") as Bitmap
            binding.iconoUsuario.setImageBitmap(foto)
            binding.iconoUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP)
            uriFoto = getUriFoto(applicationContext, foto)!!
            fotoRutaAvatar = getRutaFromUri(uriFoto)!!
        }
    }

    private val launcherAvatarGaleria: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode == RESULT_OK && result.data != null) {
            uriFoto = result.data!!.data!!

            imageDecoder(uriFoto)

            if(foto != null) {
                fotoRutaAvatar = getRutaFromUri(uriFoto)!!
                binding.iconoUsuario.setImageBitmap(BitmapFactory.decodeFile(fotoRutaAvatar))
                binding.iconoUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP)
            } else {
                Toast.makeText(this, R.string.error_cargando_imagen, Toast.LENGTH_SHORT).show()
            }
        }
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
            LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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
            Log.e("Error al decodificar imagen", "Método imageDecoder:$e")
        }
    }

    private fun getUriFoto(context: Context, image: Bitmap): Uri? {
        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "imagen_" + System.currentTimeMillis() + ".jpg")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        var imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            context.contentResolver.openOutputStream(imageUri!!).use { outputStream ->
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
            }
        } catch (e: IOException) {
            Log.e("Error en la uri", "Método getUriFoto:$e")
        }

        return imageUri
    }

    private fun getRutaFromUri(uri: Uri): String? {
        var nombreArchivo = "imagen_" + System.currentTimeMillis() + ".jpg"
        var directorioDestino = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "imagenes")

        if(!directorioDestino.exists()) {
            directorioDestino.mkdirs()
        }

        var archivoDestino = File(directorioDestino, nombreArchivo)

        try {
            contentResolver.openInputStream(uri).use { inputStream ->
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
            Log.e("Error en la ruta imagen", "Método getRutaFromUri:$e")
            return null
        }
    }

    private fun crearDialogCamara() {
        dialogCamara = MaterialAlertDialogBuilder(this)
            .setMessage(R.string.elegir_foto)
            .setNeutralButton(R.string.dialog_cancelar, null)
            .setPositiveButton("") { dialog, which ->
                if(tienePermisoCamara()) {
                    abrirCamara()
                } else {
                    pedirPermisoCamara()
                }
            }
            .setNegativeButton("") { dialog, which ->
                if(tienePermidoGaleria()) {
                    abrirGaleria()
                } else {
                    pedirPermisoGaleria()
                }
            }
            .setNegativeButtonIcon(ContextCompat.getDrawable(this, R.drawable.galeria))
            .setPositiveButtonIcon(ContextCompat.getDrawable(this, R.drawable.camara))

        dialogCamara.show()
    }

    private fun getLocalizacion(): Boolean {
        var localizacionConseguida = false

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if(location != null) {
                        latitud = location.latitude.toString()
                        longitud = location.longitude.toString()
                    } else {
                        latitud = "0.0"
                        longitud = "0.0"
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
            usuario = EntidadUsuario(nombre, correo, contrasenya, fechaNacimiento, idRol, fotoRutaAvatar, baja, "", ultimaModificacion, latitud, longitud)

            if(usuarioBBDD.insertarUsuario(usuario)) {
                startActivity(Intent(this, LoginActivity::class.java))
                limpiarCampos()
                finish()
            } else {
                binding.btnRegistrar.isEnabled = true
            }
        }, 2000)
    }

    private fun abrirCamara() {
        launcherAvatarCamara.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun abrirGaleria() {
        intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcherAvatarGaleria.launch(intent)
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
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
                        var uri =Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .show()
            }
        } else if(requestCode == REQUEST_CODE_PERMISO_CAMARA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else if(requestCode == REQUEST_CODE_PERMISO_GALERIA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}