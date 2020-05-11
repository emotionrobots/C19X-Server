package org.c19x.server.handler;

import java.io.File;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;

public class WebHandler extends ResourceHandler {

	public WebHandler(final File folder) {
		setDirAllowed(false);
		setDirectoriesListed(false);
		setBaseResource(new PathResource(folder));
	}

}
