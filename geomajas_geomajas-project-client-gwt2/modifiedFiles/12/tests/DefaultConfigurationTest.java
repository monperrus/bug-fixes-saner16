/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.caching.cache;

import com.vividsolutions.jts.geom.Envelope;
import org.geomajas.layer.VectorLayer;
import org.geomajas.plugin.caching.service.CacheCategory;
import org.geomajas.plugin.caching.service.CacheManagerServiceImpl;
import org.geomajas.plugin.caching.service.CacheServiceInfo;
import org.geomajas.plugin.caching.service.DummyCacheFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for the CacheManagerServiceImpl class.
 *
 * @author Joachim Van der Auwera
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/geomajas/spring/geomajasContext.xml",
		"/dummySecurity.xml", "/org/geomajas/testdata/layerCountries.xml",
		"/org/geomajas/testdata/layerBeans.xml"})
public class DefaultConfigurationTest {

	@Autowired
	private CacheManagerServiceImpl cacheManager;

	@Autowired
	@Qualifier("countries")
	private VectorLayer layer;

	@Autowired
	@Qualifier("beans")
	private VectorLayer otherLayer;

	private Envelope envelope;
	private Envelope overlappingEnvelope;

	@Before
	public void init() throws Exception {
		envelope = new Envelope(0, 10, 0, 10);
		overlappingEnvelope = new Envelope(5, 15, 5, 15);
	}

	@Test
	public void testSimplePutGet() throws Exception {
		String data = "data";
		String key = "123";
		String otherKey = "321";
		cacheManager.put(layer, CacheCategory.REBUILD, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key));
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key, String.class));
		Assert.assertNull(data, cacheManager.get(layer, CacheCategory.REBUILD, otherKey));
		Assert.assertNull(data, cacheManager.get(layer, CacheCategory.FEATURE, key));
		Assert.assertEquals(data, cacheManager.get(otherLayer, CacheCategory.REBUILD, key)); // both layers share the same cache
		cacheManager.remove(layer, CacheCategory.REBUILD, key);
		Assert.assertNull(cacheManager.get(layer, CacheCategory.REBUILD, key));
	}

	@Test
	public void dropOne() throws Exception {
		String data = "data";
		String key = "123";
		cacheManager.put(layer, CacheCategory.REBUILD, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key));
		cacheManager.drop(layer, CacheCategory.REBUILD);
		Assert.assertNull(cacheManager.get(layer, CacheCategory.REBUILD, key));
	}

	@Test
	public void dropLayer() throws Exception {
		String data = "data";
		String key = "123";
		cacheManager.put(layer, CacheCategory.REBUILD, key, data, envelope);
		cacheManager.put(layer, CacheCategory.FEATURE, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key));
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.FEATURE, key));
		cacheManager.drop(layer);
		Assert.assertNull(cacheManager.get(layer, CacheCategory.REBUILD, key));
		Assert.assertNull(cacheManager.get(layer, CacheCategory.FEATURE, key));
	}

	@Test
	public void invalidateOne() throws Exception {
		String data = "data";
		String key = "123";
		cacheManager.put(layer, CacheCategory.REBUILD, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key));
		cacheManager.invalidate(layer, CacheCategory.REBUILD, overlappingEnvelope);
		Assert.assertNull(cacheManager.get(layer, CacheCategory.REBUILD, key));
	}

	@Test
	public void invalidateLayer() throws Exception {
		String data = "data";
		String key = "123";
		cacheManager.put(layer, CacheCategory.REBUILD, key, data, envelope);
		cacheManager.put(layer, CacheCategory.FEATURE, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.REBUILD, key));
		Assert.assertEquals(data, cacheManager.get(layer, CacheCategory.FEATURE, key));
		cacheManager.invalidate(layer, overlappingEnvelope);
		Assert.assertNull(cacheManager.get(layer, CacheCategory.REBUILD, key));
		Assert.assertNull(cacheManager.get(layer, CacheCategory.FEATURE, key));
	}

	@Test
	public void invalidateNoIndexLayer() throws Exception {
		String data = "data";
		String key = "123";
		cacheManager.put(otherLayer, CacheCategory.REBUILD, key, data, envelope);
		cacheManager.put(otherLayer, CacheCategory.FEATURE, key, data, envelope);
		Assert.assertEquals(data, cacheManager.get(otherLayer, CacheCategory.REBUILD, key));
		Assert.assertEquals(data, cacheManager.get(otherLayer, CacheCategory.FEATURE, key));
		cacheManager.invalidate(otherLayer, overlappingEnvelope);
		Assert.assertNull(cacheManager.get(otherLayer, CacheCategory.REBUILD, key));
		Assert.assertNull(cacheManager.get(otherLayer, CacheCategory.FEATURE, key));
	}
}
