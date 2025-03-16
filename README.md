# 📚 Codex Libris
**Proyecto final del Ciclo Formativo de Grado Superior DAM - En Desarrollo**

![Codex Libris](https://via.placeholder.com/800x400?text=Codex+Libris)

## 📌 Sobre el Proyecto
**Codex Libris** es el proyecto final de mi **Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM)**. Se trata de una aplicación de escritorio en desarrollo que busca facilitar la gestión de una biblioteca municipal.

Este software está siendo construido con **JavaFX** y **Maven**, utilizando **Scene Builder** para la interfaz gráfica y conectándose a un servidor mediante HTTP para la autenticación y gestión de datos.

## 🚀 Funcionalidades (En Desarrollo)
✅ **Gestión de usuarios**: Inicio de sesión, creación de cuentas y control de accesos según rol (administrador/usuario).  
✅ **Administración de libros**: Agregar, editar y eliminar libros de la base de datos.  
✅ **Gestión de préstamos**: Control de libros prestados y fechas de devolución.  
✅ **Gestión de eventos**: Registro de actividades dentro de la biblioteca.  
✅ **Panel de administración**: Sección exclusiva para administradores donde pueden gestionar todos los aspectos de la biblioteca.  
✅ **Notificaciones**: Sistema de alertas para eventos y actividades.  
✅ **Interfaz moderna y amigable**: Construida con JavaFX y Scene Builder.  

💡 *Nota:* Algunas funcionalidades aún están en fase de desarrollo y pueden estar sujetas a cambios.

## 🛠️ Tecnologías Utilizadas
- **Java 17**
- **JavaFX** para la interfaz gráfica
- **Maven** como gestor de dependencias
- **Scene Builder** para la creación de interfaces
- **GitHub** para control de versiones
- **HTTP Requests** para conexión con el servidor

## 📂 Estructura del Proyecto
```
CodexLibris/
│── src/main/java/com/codexteam/codexlib/
│   ├── PantallaInicial.java   # Punto de entrada de la aplicación
│   ├── LoginController.java   # Controlador de la pantalla de login
│   ├── AdminController.java   # Panel de administración
│   ├── ConnexioServidor.java  # Conexión con el servidor mediante HTTP
│── src/main/resources/com/codexteam/codexlib/
│   ├── fxml/                  # Archivos FXML de la interfaz
│   ├── images/                # Iconos e imágenes de la app
│── pom.xml                    # Configuración de Maven
│── README.md                  # Este documento
```

## 🎮 Capturas de Pantalla
🚧 *Próximamente* 🚧

## 🔧 Instalación y Ejecución
### **Requisitos previos**
- Tener instalado **Java 17 o superior**
- Tener **Maven** instalado y configurado

### **Clonar el repositorio**
```bash
git clone https://github.com/tuusuario/codex-libris.git
cd codex-libris
```

### **Compilar y ejecutar**
```bash
mvn clean javafx:run
```

## 📌 Contribuciones
Dado que **Codex Libris** es un proyecto académico en desarrollo, las contribuciones externas están limitadas. Sin embargo, cualquier sugerencia o comentario será bien recibido.

Si quieres aportar ideas o reportar errores, puedes abrir un **issue** en GitHub.

## 📝 Licencia
Este proyecto está bajo la **Licencia MIT**. Puedes usarlo libremente con atribución.

