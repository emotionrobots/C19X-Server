package org.c19x.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileUtil {
	private final static String tag = FileUtil.class.getName();

	/**
	 * Notify consumer on file content change.
	 * 
	 * @param file
	 * @param consumer
	 */
	public final static void onChange(final File file, final Consumer<String> consumer) {
		final Path path = file.toPath();
		final Thread thread = new Thread() {
			private long timestamp = 0;
			private String content;

			@Override
			public void run() {
				while (true) {
					try {
						if (file.exists() && file.canRead()) {
							if (file.lastModified() > timestamp) {
								final String newContent = new String(Files.readAllBytes(path));
								if (content == null || !content.equals(newContent)) {
									timestamp = file.lastModified();
									content = newContent;
									Logger.debug(tag, "File updated (file={})", file);
									consumer.accept(content);
								}
							}
						}
						sleep(4000);
					} catch (Throwable e) {
						Logger.warn(tag, "On change monitor failed (file={})", file, e);
					}
				}
			}
		};
		thread.start();
	}
}
