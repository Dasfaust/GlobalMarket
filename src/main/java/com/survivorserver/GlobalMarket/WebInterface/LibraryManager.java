package com.survivorserver.GlobalMarket.WebInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import com.survivorserver.GlobalMarket.Market;

public class LibraryManager {

	public static boolean loadLibraries(Market market) {
		try {
			/*List<String> libraries = new ArrayList<String>();
			libraries.add("jackson-core");
			libraries.add("jackson-databind");
			libraries.add("jackson-annotations");
			for (String library : libraries) {
				File file = new File("lib/" + library + "-2.1.4.jar");
				if (!file.exists()) {
					market.getLogger().info("Downloading " + library + "-2.1.4.jar...");
					URL site = new URL("http://repo1.maven.org/maven2/com/fasterxml/jackson/core/" + library + "/2.1.4/" + library + "-2.1.4.jar");
					ReadableByteChannel channel = Channels.newChannel(site.openStream());
					FileOutputStream fos = new FileOutputStream("lib/" + library + "-2.1.4.jar");
					fos.getChannel().transferFrom(channel,  0,  1 << 24);
					fos.close();
				}
			}*/
			
			List<JarFile> libs = new ArrayList<JarFile>();
			File core = new File("lib/jackson-core-2.1.4.jar");
			File databind = new File("lib/jackson-databind-2.1.4.jar");
			File annotations = new File("lib/jackson-annotations-2.1.4.jar");
			if (!core.exists() || !databind.exists() || !annotations.exists()) {
				throw new FileNotFoundException();
			}
			libs.add(new JarFile(core));
			libs.add(new JarFile(databind));
			libs.add(new JarFile(annotations));
			for (JarFile library : libs) {
				URL[] urls = { new URL("jar:" + new File(library.getName()).toURI().toURL().toExternalForm() + "!/") };
				for (int i = 0; i < urls.length; i++) {
					URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		        	Class<URLClassLoader> sysclass = URLClassLoader.class;
		        	Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
		        	method.setAccessible(true);
		        	method.invoke(sysloader, new Object[] { urls[i] });
				}
			}
			return true;
		} catch(Exception e) {
			market.getLogger().warning("Could not load Jackson library:");
			e.printStackTrace();
			return false;
		}
	}
}
