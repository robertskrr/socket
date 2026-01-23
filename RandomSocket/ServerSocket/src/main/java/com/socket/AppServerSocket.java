package com.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class AppServerSocket {
	private final static int PORT = 7777;
	private static int numGen;
	private static int intentos = 0;

	public static void main(String[] args) {
		// Generar número
		numGen = (new Random()).nextInt(10) + 1;

		try {
			ServerSocket srvSock = new ServerSocket(PORT);
			System.out.println("ServerSocket en puerto: " + PORT);

			// Abrimos el socket y escuchamos
			Socket client = srvSock.accept();
			mostrarInfoCliente(client);

			// Para mandar datos al cliente >>>
			PrintWriter salida = new PrintWriter(client.getOutputStream(), true);
			// Para recibir datos al cliente <<<
			BufferedReader entrada = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			// Saludo del servidor mostrando reglas del juego
			salida.println("<Server> Conectado con éxito al juego 'Número mágico'. Adivina el número (1-10) o escribe 'salir' para finalizar el juego");
			
			// Establecemos a cero los intentos por si acaso
			intentos = 0;

			String datoRec, datoEnv;
			// leeremos todos los mensajes recibidos
			// comprobamos si es el número mágico
			while ((datoRec = entrada.readLine()) != null) {
				if (datoRec.contains("salir")) {
					salida.println("Juego finalizado :(");
					break;
				}
				intentos++;
				datoEnv = checkNumero(datoRec);
				// Retornamos al cliente el resultado
				// de la comprobación
				salida.println(datoEnv);

				// Si contiene la palabra FIN cierra la conexión
				if (datoEnv.contains("FIN")) {
					break;
				}
			}

			// Cerramos todo
			salida.close();
			entrada.close();
			srvSock.close();
			client.close();

		} catch (IOException e) {
			System.err.println("Problemas en el socket");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void mostrarInfoCliente(Socket client) {
		// Obtener la IP del cliente
		InetAddress clientAddress = client.getInetAddress();
		String clientIP = clientAddress.getHostAddress();
		String hostName = clientAddress.getHostName();

		System.out.println("CLIENTE CONECTADO -- IP:" + clientIP + ", HostName: " + hostName);
	}

	private static String checkNumero(String datoRec) {
		try {
			int numero = Integer.parseInt(datoRec);

			if (numero > numGen) {
				return "<server>El número es mayor que el número mágico";
			} else if (numero < numGen) {
				return "<server>El número es menor que el número mágico";
			} else { // Muestra los intentos
				return "<server>Ha adivinado el número. FIN DEL JUEGO. -- ¡ENHORABUENA! HAS GANADO CON " + intentos
						+ " INTENTOS.";
			}

		} catch (NumberFormatException e) {
			return "<Server>Por favor, introduzca un número";
		}

	}
}
