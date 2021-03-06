package imagej.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class DoubleArrayAccessorTest {

	private DoubleArrayAccessor accessor;
	
	@Test
	public void testByteArrayAccessor() {
		double[] data = new double[]{1.1,2.2,3.3,4.4};
		this.accessor = new DoubleArrayAccessor(data);
		assertNotNull(this.accessor);
	}

	@Test
	public void testGetReal() {
		double[] data = new double[]{1.1,2.2,3.3,4.4};
		this.accessor = new DoubleArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1.1, this.accessor.getReal(0), 0);
		assertEquals(2.2, this.accessor.getReal(1), 0);
		assertEquals(3.3, this.accessor.getReal(2), 0);
		assertEquals(4.4, this.accessor.getReal(3), 0);
	}

	@Test
	public void testGetIntegral() {
		double[] data = new double[]{1.1,2.2,3.3,4.4};
		this.accessor = new DoubleArrayAccessor(data);
		assertNotNull(this.accessor);
		assertEquals(1, this.accessor.getIntegral(0));
		assertEquals(2, this.accessor.getIntegral(1));
		assertEquals(3, this.accessor.getIntegral(2));
		assertEquals(4, this.accessor.getIntegral(3));
	}

	@Test
	public void testSetReal() {
		double[] data = new double[]{1.1,2.2,3.3,4.4,5.5};
		this.accessor = new DoubleArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setReal(0, -Double.MAX_VALUE);
		assertEquals(-Double.MAX_VALUE, this.accessor.getReal(0), 0);
		
		this.accessor.setReal(1, -1.93);
		assertEquals(-1.93, this.accessor.getReal(1), 0);
		
		this.accessor.setReal(2, 0);
		assertEquals(0, this.accessor.getReal(2), 0);
		
		this.accessor.setReal(3, 14.445);
		assertEquals(14.445, this.accessor.getReal(3), 0);
		
		this.accessor.setReal(4, Double.MAX_VALUE);
		assertEquals(Double.MAX_VALUE, this.accessor.getReal(4), 0);
	}

	@Test
	public void testSetIntegral() {
		double[] data = new double[]{1.1,2.2,3.3,4.4,5.5};
		this.accessor = new DoubleArrayAccessor(data);
		assertNotNull(this.accessor);
		
		this.accessor.setIntegral(0, (long)-Double.MAX_VALUE);
		assertEquals((long)-Double.MAX_VALUE, this.accessor.getIntegral(0));
		
		this.accessor.setIntegral(1, -1);
		assertEquals(-1, this.accessor.getIntegral(1));
		
		this.accessor.setIntegral(2, 0);
		assertEquals(0, this.accessor.getIntegral(2));
		
		this.accessor.setIntegral(3, 14);
		assertEquals(14, this.accessor.getIntegral(3));
		
		this.accessor.setIntegral(4, (long)Double.MAX_VALUE);
		assertEquals((long)Double.MAX_VALUE, this.accessor.getIntegral(4));
	}

}
