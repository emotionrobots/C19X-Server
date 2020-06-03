package org.c19x.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.c19x.server.data.Devices;
import org.c19x.server.data.InfectionData;
import org.c19x.server.data.Parameters;
import org.c19x.server.handler.ControlHandler;
import org.c19x.server.handler.InfectionDataHandler;
import org.c19x.server.handler.MessageHandler;
import org.c19x.server.handler.ParametersHandler;
import org.c19x.server.handler.RegistrationHandler;
import org.c19x.server.handler.StatusHandler;
import org.c19x.server.handler.TimeHandler;
import org.c19x.server.handler.WebHandler;
import org.c19x.util.FileUtil;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class C19XHttpsServer {
	private final static String tag = C19XHttpsServer.class.getName();
	private final Server server;
	private final Map<String, Handler> handlers = new HashMap<>();

	public C19XHttpsServer(final int port, final File p12KeystoreFile, final File keystorePasswordFile)
			throws UnsupportedEncodingException, IOException {
		this.server = new Server();
		enableHTTPS(server, port, p12KeystoreFile, keystorePasswordFile);
	}

	public Map<String, Handler> getHandlers() {
		return handlers;
	}

	public void start() throws Exception {
		final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection(
				handlers.entrySet().stream().map(e -> {
					final ContextHandler contextHandler = new ContextHandler("/" + e.getKey());
					contextHandler.setHandler(e.getValue());
					return contextHandler;
				}).collect(Collectors.toList()).toArray(new ContextHandler[handlers.size()]));
		server.setHandler(contextHandlerCollection);
		server.start();
		server.join();
	}

	/**
	 * Enable HTTP server. Remember to setup port forwarding on router.
	 * 
	 * @param jettyServer
	 * @param port        Default port for HTTP is 8080
	 */
	private final static void enableHTTP(final Server jettyServer, final int port) {
		final HttpConfiguration config = new HttpConfiguration();
		config.addCustomizer(new SecureRequestCustomizer());
		config.addCustomizer(new ForwardedRequestCustomizer());

		final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
		final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnectionFactory);
		httpConnector.setPort(port); // IP tables redirect 80 -> 8080
		jettyServer.addConnector(httpConnector);

		Logger.info(tag,
				"Enabled HTTP server on port {}, remember to setup port forwarding on router for port 80 to {}", port,
				port);
	}

	/**
	 * Enable HTTPS server. Remember to setup port forwarding on router.<br>
	 * 1. Setup port forwarding on router for port 8080 (local) to 80 (remote)<br>
	 * 2. Setup DNS on domain registration for A record to point domain to external
	 * IP address<br>
	 * 3. Use Let's Encrypt to generate PEM file for domain<br>
	 * 4. Create keystore password file in /opt/domain/keystore.pw<br>
	 * 5. Convert PEM file to P12 file using OpenSSL (script convertPEMtoP12)<br>
	 * 
	 * @param jettyServer
	 * @param port                 Default port for HTTPS is 8443
	 * @param p12KeystoreFile
	 * @param keystorePasswordFile
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private final static void enableHTTPS(final Server jettyServer, final int port, final File p12KeystoreFile,
			final File keystorePasswordFile) throws UnsupportedEncodingException, IOException {
		final HttpConfiguration config = new HttpConfiguration();
		config.addCustomizer(new SecureRequestCustomizer());
		config.addCustomizer(new ForwardedRequestCustomizer());

		final SslContextFactory sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStoreType("PKCS12");
		sslContextFactory.setKeyStorePath(p12KeystoreFile.toString());
		final String keyStorePassword = new String(Files.readAllBytes(keystorePasswordFile.toPath()), "UTF-8").trim();
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		sslContextFactory.setKeyManagerPassword(keyStorePassword);
		final ServerConnector httpsConnector = new ServerConnector(jettyServer, sslContextFactory,
				new HttpConnectionFactory(config));
		httpsConnector.setPort(port); // IP tables redirect 8443 -> 443
		httpsConnector.setIdleTimeout(5 * 60000);
		jettyServer.addConnector(httpsConnector);

		Logger.info(tag,
				"Enabled HTTPS server on port {}, remember to setup port forwarding on router for port 443 to {}", port,
				port);
	}

	public static void main(String[] args) throws Exception {
		final int port = Integer.parseInt(args[0]);
		final File p12KeystoreFile = new File(args[1]);
		final File keystorePasswordFile = new File(args[2]);
		final File parametersFile = new File(args[3]);
		final File databaseFolder = new File(args[4]);
		final File webFolder = new File(args[5]);

		final Devices devices = new Devices(databaseFolder);
		final Parameters parameters = new Parameters(parametersFile);

		final C19XHttpsServer server = new C19XHttpsServer(port, p12KeystoreFile, keystorePasswordFile);
		final ParametersHandler parametersHandler = new ParametersHandler();
		final InfectionDataHandler infectionDataHandler = new InfectionDataHandler();
		final ControlHandler controlHandler = new ControlHandler(devices, parameters, infectionDataHandler);
		final AtomicReference<Timer> infectionDataUpdateTimer = new AtomicReference<>(
				infectionDataUpdateTimer(devices, parameters, infectionDataHandler));

		server.getHandlers().put("", new WebHandler(webFolder));
		server.getHandlers().put("time", new TimeHandler());
		server.getHandlers().put("registration", new RegistrationHandler(devices));
		server.getHandlers().put("status", new StatusHandler(devices));
		server.getHandlers().put("message", new MessageHandler(devices));
		server.getHandlers().put("infectionData", infectionDataHandler);
		server.getHandlers().put("parameters", parametersHandler);
		server.getHandlers().put("control", controlHandler);

		FileUtil.onChange(parametersFile, json -> {
			parameters.fromJSON(json);
			parametersHandler.set(parameters);
			infectionDataUpdateTimer.get().cancel();
			infectionDataUpdateTimer.set(infectionDataUpdateTimer(devices, parameters, infectionDataHandler));
		});

		server.start();
	}

	private final static Timer infectionDataUpdateTimer(final Devices devices, final Parameters parameters,
			final InfectionDataHandler infectionDataHandler) {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				infectionDataHandler.set(new InfectionData(devices, parameters.getRetention()));
			}
		}, 0, ((long) parameters.getUpdate()) * 60 * 1000);
		return timer;
	}
}
