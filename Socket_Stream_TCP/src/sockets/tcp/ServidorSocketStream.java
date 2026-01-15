package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class ServidorSocketStream {

	public static void main(String[] args) {
		System.out.println("Creando socket servidor");
		try (ServerSocket serverSocket = new ServerSocket()) {
			System.out.println("Realizando el bind");

			InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
			serverSocket.bind(addr);

			System.out.println("Aceptando conexiones");

			try (Socket newSocket = serverSocket.accept()) {
				System.out.println("Conexión recibida");

				InputStream is = newSocket.getInputStream();
				OutputStream os = newSocket.getOutputStream();

				byte[] mensaje = new byte[25];
				// PROBLEMA TÍPICO
				/*
				 * is.read(mensaje); // Si el mensaje es "Hola" (4 bytes)
				 * 
				 * System.out.println("Mensaje recibido: " + new String(mensaje)); // "Hola" +
				 * bytes basura
				 */

				// SOLUCIÓN
				int leidos = is.read(mensaje);
				System.out.println("Mensaje recibido: " + new String(mensaje, 0, leidos)); // "Hola" sin bytes basura
				
				// No hace falta .close() ya que al salir del try se cierra la conexión
			} 

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Terminado");
	}
}