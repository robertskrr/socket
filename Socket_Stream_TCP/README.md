<h1>EJERCICIO 1</h1>
La IP de la VM la obtuve con el comando <b>hostname -I</b>, ya que trabajo desde Linux.
<br><br>

<b>- IP del Servidor:</b> Configurada para escuchar en 0.0.0.0.
<br>
<b>- IP del Cliente:</b> IP de la VM (ej: 10.0.2.15).
<br>
<b>- Puerto:</b> Seleccionado manualmente (6666).
<br><br>

<h3>Cómo ejecutar:</h3>
Desde la carpeta Socket_Stream_TCP/src ejecuta en la terminal: 
<br><br>
- javac sockets/tcp/ServidorSocketStream.java (Compila .java del servidor)
<br>
- java sockets.tcp.ServidorSocketStream (Ejecuta el .java)
<br><br>
<img src="./img/01_server.jpeg" alt="Captura 1" width="500"/> 
<br><br>
En otra terminal ejecuta, desde la misma ruta anterior:
<br><br>
- javac sockets/tcp/ClienteSocketStream.java (Compila .java del cliente)
<br>
- java sockets.tcp.ClienteSocketStream (Ejecuta el .java)
<br><br>
<img src="./img/02_conexion.jpeg" alt="Captura 2" width="500"/> 