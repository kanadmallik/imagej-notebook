/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2017 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.notebook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link NotebookService}.
 *
 * @author Curtis Rueden
 */
public class NotebookServiceTest {

	private Context context;
	private NotebookService ns;
	private DatasetService ds;

	@Before
	public void setUp() {
		context = new Context(NotebookService.class, DatasetService.class);
		ns = context.service(NotebookService.class);
		ds = context.service(DatasetService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/** Tests {@link NotebookService#display(Dataset)}. */
	@Test
	public void testDisplayDataset() {
		final ArrayImg<UnsignedByteType, ByteArray> img = createTestImg();
		final Dataset dataset = ds.create(img);
		final Object rendered = ns.display(dataset);
		assertSameImageDetails(img, rendered);
	}

	/** Tests {@link NotebookService#display(RandomAccessibleInterval)}. */
	@Test
	public void testDisplayRAI() {
		final ArrayImg<UnsignedByteType, ByteArray> img = createTestImg();
		final Object rendered = ns.display(img);
		assertSameImageDetails(img, rendered);
	}

	@Test
	public void testMethods() {
		final NotebookTable table = ns.methods(java.lang.Object.class);
		assertEquals(9, table.size());
		assertRow(table.get(0), "equals", "java.lang.Object", "boolean");
		assertRow(table.get(1), "getClass", "<none>", "java.lang.Class");
		assertRow(table.get(2), "hashCode", "<none>", "int");
		assertRow(table.get(3), "notify", "<none>", "void");
		assertRow(table.get(4), "notifyAll", "<none>", "void");
		assertRow(table.get(5), "toString", "<none>", "java.lang.String");
		assertRow(table.get(6), "wait", "<none>", "void");
		assertRow(table.get(7), "wait", "long", "void");
		assertRow(table.get(8), "wait", "long, int", "void");
	}

	// -- Helper methods --

	private ArrayImg<UnsignedByteType, ByteArray> createTestImg() {
		final int w = 20, h = 20, valueOffset = 23;
		final byte[] data = new byte[w * h];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (valueOffset + i);
		}
		return ArrayImgs.unsignedBytes(data, w, h);
	}

	private void assertSameImageDetails(
		final ArrayImg<UnsignedByteType, ByteArray> img, final Object rendered)
	{
		assertTrue(rendered instanceof BufferedImage);
		final BufferedImage bi = (BufferedImage) rendered;

		final long w = img.dimension(0);
		final long h = img.dimension(1);
		assertEquals(w, bi.getWidth());
		assertEquals(h, bi.getHeight());

		final byte[] data = img.update(null).getCurrentStorageArray();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				final int index = (int) (y * w + x);
				final int value = data[index] & 0xff;
				final int rgb = bi.getRGB(x, y);
				final int r = rgb & 0xff;
				final int g = (rgb << 8) & 0xff;
				final int b = (rgb << 16) & 0xff;
				final int a = (rgb << 24) & 0xff;
				assertEquals(value, r);
				assertEquals(0, g);
				assertEquals(0, b);
				assertEquals(0, a);
			}
		}
	}

	private void assertRow(final LinkedHashMap<String, Object> row1,
		final String name, final String arguments, final String returns)
	{
		assertEquals(name, row1.get("name"));
		assertEquals(arguments, row1.get("arguments"));
		assertEquals(returns, row1.get("returns"));
	}
}
