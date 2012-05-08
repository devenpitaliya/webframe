
package org.webframe.support.driver.resource.jar;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.webframe.support.util.ClassUtils;

/**
 * JarResource资源类，用于匹配搜索jar包中的资源文件
 * 
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆 </a>
 * @version $Id: codetemplates.xml,v 1.1 2009/09/07 08:48:12 Exp $ Create: 2011-4-6 下午04:40:09
 */
public class JarResourcePatternResolver
			extends
				PathMatchingResourcePatternResolver {

	private Class<?>				jarClass				= null;

	private JarURLConnection	jarCon				= null;

	private JarResourceLoader	jarResourceLoader	= null;

	public JarResourcePatternResolver(Class<?> jarClass) throws IOException {
		this(new JarResourceLoader(jarClass));
		this.jarClass = jarClass;
	}

	public JarResourcePatternResolver(JarURLConnection jarURLConnection)
				throws IOException {
		this(new JarResourceLoader(jarURLConnection));
		this.jarCon = jarURLConnection;
	}

	private JarResourcePatternResolver(JarResourceLoader jarResourceLoader) {
		super(jarResourceLoader);
		this.jarResourceLoader = jarResourceLoader;
	}

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		if (jarCon != null || ClassUtils.isInJar(this.jarClass)) {
			return findPathMatchingJarResources(locationPattern);
		} else {
			Resource classRoot = ClassUtils.getClassesRootResource(this.jarClass);
			String directory = determineRootDir(locationPattern);
			String pattern = locationPattern.replaceAll(directory, "");
			Resource dirRoot = classRoot.createRelative(directory);
			if (dirRoot.exists()) {
				List<Resource> finder = new ArrayList<Resource>();
				Collection<?> files = FileUtils.listFiles(dirRoot.getFile(), null,
					true);
				for (Object object : files) {
					if (!(object instanceof File)) {
						continue;
					}
					File file = (File) object;
					String filename = file.getName();
					if (getPathMatcher().match(pattern, filename)) {
						finder.add(new FileSystemResource(file));
					}
				}
				return finder.toArray(new Resource[finder.size()]);
			} else {
				return null;
			}
		}
	}

	public JarResourceLoader getJarResourceLoader() {
		return this.jarResourceLoader;
	}

	protected Resource[] findPathMatchingJarResources(String locationPattern) {
		List<Resource> result = new ArrayList<Resource>(16);
		JarResourceLoader jarResourceLoader = (JarResourceLoader) getResourceLoader();
		Set<String> entriesPath = jarResourceLoader.getEntryFilesByDir(
			locationPattern, getPathMatcher());
		if (entriesPath == null) {
			return result.toArray(new Resource[result.size()]);
		}
		for (String entryPath : entriesPath) {
			String path = "/" + entryPath;
			if (getPathMatcher().match(locationPattern, path)) {
				Resource resource = getResource(path);
				if (resource == null) {
					continue;
				}
				result.add(resource);
			}
		}
		return result.toArray(new Resource[result.size()]);
	}
}
