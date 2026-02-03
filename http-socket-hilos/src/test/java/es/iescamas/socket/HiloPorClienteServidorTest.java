package es.iescamas.socket;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Tests (tipo integración): se conectan con sockets reales al servidor. -
 * Arrancamos el servidor antes de cada test. - Hacemos peticiones HTTP mínimas
 * por socket. - Validamos la respuesta.
 */
class HiloPorClienteServidorTest {

	private HiloPorClienteServidor server; // servidor bajo prueba
	private Thread serverThread; // hilo donde corre el servidor
	private int port; // puerto elegido automáticamente

	@BeforeEach
	void startServer() throws Exception {
		// (1) Pedimos un puerto libre al SO usando un ServerSocket temporal en 0
		// Esto evita conflictos típicos: "Address already in use".
		try (ServerSocket tmp = new ServerSocket(0)) {
			port = tmp.getLocalPort();
		}

		// (2) Creamos el servidor con ese puerto y lo arrancamos en un hilo,
		// igual que en el Main de la aplicación.
		server = new HiloPorClienteServidor(port);
		serverThread = new Thread(server, "test-server");
		serverThread.start();

		// (3) Esperamos un poco hasta que el servidor esté escuchando.
		// Si tu equipo va lento, este método evita falsos fallos.
		waitUntilListening("127.0.0.1", port, 800);
	}

	@AfterEach
	void stopServer() throws Exception {
		// (4) Paramos el servidor y damos margen para que el hilo termine.
		server.stop();
		serverThread.join(500);
	}

	@Test
	@DisplayName("GET /nombre/Ana devuelve 200 OK y 'Hola Ana'")
	@Timeout(value = 2, unit = TimeUnit.SECONDS) // evita cuelgues si algo falla
	@Tag("http")
	void shouldSayHelloFromNombreRoute() throws Exception {
		// (5) Petición real
		String response = httpGet("/nombre/Ana");

		// (6) Assertions: estado HTTP y contenido
		assertTrue(response.contains("200 OK"), "Debe devolver 200 OK");
		assertTrue(response.contains("Hola Ana"), "Debe contener el saludo");
	}

	@Test
	@DisplayName("GET /noexiste devuelve 404 Not Found")
	@Tag("http")
	void shouldReturn404ForUnknownRoute() throws Exception {
		// Petición a ruta que no está definida
		String response = httpGet("/noexiste");

		// Verificamos que devuelva 404 Not Found
		assertTrue(response.contains("404 Not Found"), "Debe devolver el código de estado 404");
		// Verificamos que contenga un mensaje HTML de error
		assertTrue(response.contains("La página no existe"), "Debe mostrar el mensaje HTML de error");
	}

	@Test
	@DisplayName("Concurrencia: 2 clientes simúltaneos reciben saludos")
	@Tag("concurrencia")
	void shouldHandleConcurrentClients() throws Exception {
		// Contenedores para las respuestas
		final String[] res1 = { "" };
		final String[] res2 = { "" };

		// 2 hilos para los clientes
		Thread cliente1 = new Thread(() -> {
			try {
				res1[0] = httpGet("/nombre/Ana");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		});
		
		Thread cliente2 = new Thread(() -> {
			try {
				res2[0] = httpGet("/nombre/Pepe");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		});
		
		// Arrancamos los hilos a la vez
		cliente1.start();
		cliente2.start();
		
		// Esperamos a que terminen
		cliente1.join();
		cliente2.join();
		
		// Comprobamos que cada uno recibió correctamente el saludo
		assertTrue(res1[0].contains("Hola Ana"), "El cliente 1 debe ver 'Hola Ana'");
		assertTrue(res2[0].contains("Hola Pepe"), "El cliente 2 debe ver 'Hola Pepe'");
		
		// Comprobamos que el servidor usó hilos distintos por cada cliente extrayendo el nombre del HTML
		// 1. Cortamos el HTML por "Hilo:</span> " y nos quedamos con la parte derecha [1]
		// 2. De esa parte volvemos a cortar por "</p> y nos quedamos la parte izquierda [0]
		// Resultado --> Obtenemos solo el texto con el ID del hilo
		String idHilo1 = res1[0].split("Hilo:</span> ")[1]
				.split("</p>")[0];
		String idHilo2 = res2[0].split("Hilo:</span> ")[1]
				.split("</p>")[0];
		
		// Comprobamos que cada hilo es distinto del otro
		assertNotEquals(idHilo1, idHilo2, "El servidor debe atender a cada cliente con un hilo diferente");
	}

	/**
	 * Realiza un GET HTTP mínimo por socket. Nota importante: - "Connection: close"
	 * fuerza a que el servidor cierre la conexión y podamos leer todo.
	 */
	private String httpGet(String path) throws Exception {
		try (Socket s = new Socket("127.0.0.1", port);
				OutputStream out = s.getOutputStream();
				InputStream in = s.getInputStream()) {

			String req = "GET " + path + " HTTP/1.1\r\n" + "Host: localhost\r\n" + "Connection: close\r\n" + "\r\n";

			out.write(req.getBytes(StandardCharsets.US_ASCII));
			out.flush();

			// Leemos TODO lo que responda el servidor (cabeceras + body)
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Espera a que el servidor acepte conexiones. - Reintenta durante maxMs. - Si
	 * no lo consigue, falla el test con un mensaje claro.
	 */
	private void waitUntilListening(String host, int port, long maxMs) throws Exception {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < maxMs) {
			try (Socket ignored = new Socket(host, port)) {
				return; // conectó: el servidor ya está listo
			} catch (IOException e) {
				Thread.sleep(50); // reintento corto
			}
		}
		fail("El servidor no abrió el puerto a tiempo");
	}
}