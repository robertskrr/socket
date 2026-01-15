<h1>EJERCICIO 2</h1>
Lectura correcta: bytes leídos y explicación del "buffer basura".
<br><br>
<h2> Tabla: Problema / Causa / Solución </h2>

<table>
    <thead>
        <tr>
            <th align="left">Problema</th>
            <th align="left">Causa</th>
            <th align="left">Solución</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><b>Basura al final</b></td>
            <td>El buffer es mayor que el texto enviado. El resto contiene datos vacíos o nulos.</td>
            <td>Usar el constructor <b>new String(buffer, 0, leidos)</b> para procesar solo los bytes útiles.</td>
        </tr>
        <tr>
            <td><b>Mensaje cortado</b></td>
            <td>El buffer definido es más pequeño que el mensaje enviado por el cliente.</td>
            <td>Bucle de lectura (<b>while</b>), acumular el texto en una variable o delimitar por líneas (<b>\n</b>).<td>
        </tr>
    </tbody>
</table>

<h3>Cómo ejecutar:</h3>
Desde la carpeta Socket_Stream_TCP/src ejecuta en la terminal: 
<br><br>
- javac sockets/tcp/ServidorSocketStream.java (Compila .java del servidor)
<br>
- java sockets.tcp.ServidorSocketStream (Ejecuta el .java)
<br><br>
En otra terminal ejecuta, desde la misma ruta anterior:
<br><br>
- javac sockets/tcp/ClienteSocketStream.java (Compila .java del cliente)
<br>
- java sockets.tcp.ClienteSocketStream (Ejecuta el .java)