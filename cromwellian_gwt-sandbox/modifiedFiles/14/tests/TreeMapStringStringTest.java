/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.emultest.java.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tests <code>TreeMap</code> with Strings and the natural comparator.
 */
public class TreeMapStringStringTest extends TreeMapTest<String, String> {

  public void testHeadMapEqualsFirst() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap = sortedMap.headMap("aa");
    assertEquals("length", 0, subMap.size());
    Set<String> kset = subMap.keySet();
    Iterator<String> it = kset.iterator();
    assertFalse("iterator[0]", it.hasNext());
  }

  public void testHeadMapEqualsSecond() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap = sortedMap.headMap("bb");
    assertEquals("length", 1, subMap.size());
    Set<String> kset = subMap.keySet();
    Iterator<String> it = kset.iterator();
    assertTrue("iterator[0]", it.hasNext());
    assertEquals("iterator[0]", "aa", it.next());
    assertFalse("iterator[1]", it.hasNext());
    assertEquals("firstKey", "aa", subMap.firstKey());
    assertEquals("lastKey", "aa", subMap.lastKey());
  }

  /**
   * Perform some tests on submap that are hard to do without specific knowledge
   * of the keys used.
   */
  public void testHeadMapSimple() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap = sortedMap.headMap("bz");
    assertEquals("length", 2, subMap.size());
    Set<String> kset = subMap.keySet();
    Iterator<String> it = kset.iterator();
    assertTrue("iterator[0]", it.hasNext());
    assertEquals("iterator[0]", "aa", it.next());
    assertTrue("iterator[1]", it.hasNext());
    assertEquals("iterator[1]", "bb", it.next());
    assertFalse("iterator[2]", it.hasNext());
    // issue 2638
    assertEquals("firstKey", "aa", subMap.firstKey());
    assertEquals("lastKey", "bb", subMap.lastKey());
  }

  /**
   * Tests that compositing submap operations function as expected.
   */
  public void testSubMapComposite() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap1 = sortedMap.headMap("cz").tailMap("bb");
    SortedMap<String, String> subMap2 = sortedMap.tailMap("bb").headMap("cz");
    SortedMap<String, String> subMap3 = sortedMap.subMap("bb", "cz");
    assertEquals("headMap(tailMap) should equal tailMap(headMap)", subMap1,
        subMap2);
    assertEquals("headMap(tailMap) should equal subMap", subMap1, subMap3);
    assertEquals("headMap(tailMap) size", 2, subMap1.size());
  }

  public void testTailMapPastEnd() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap = sortedMap.tailMap("dz");
    assertEquals("length", 0, subMap.size());
    Set<String> kset = subMap.keySet();
    Iterator<String> it = kset.iterator();
    assertFalse("iterator[0]", it.hasNext());
    try {
      subMap.firstKey();
      fail("firstKey should have thrown NoSuchElementException");
    } catch (NoSuchElementException expected) {
    }
    try {
      subMap.lastKey();
      fail("lastKey should have thrown NoSuchElementException");
    } catch (NoSuchElementException expected) {
    }
  }

  public void testTailMapSimple() {
    SortedMap<String, String> sortedMap = createKnownKeysMap();
    SortedMap<String, String> subMap = sortedMap.tailMap("bb");
    assertEquals("length", 3, subMap.size());
    Set<String> kset = subMap.keySet();
    Iterator<String> it = kset.iterator();
    assertTrue("iterator[0]", it.hasNext());
    assertEquals("iterator[0]", "bb", it.next());
    assertTrue("iterator[1]", it.hasNext());
    assertEquals("iterator[1]", "cc", it.next());
    assertTrue("iterator[2]", it.hasNext());
    assertEquals("iterator[2]", "dd", it.next());
    assertFalse("iterator[3]", it.hasNext());
    // issue 2638
    assertEquals("firstKey", "bb", subMap.firstKey());
    assertEquals("lastKey", "dd", subMap.lastKey());
  }

  // checks for compatibility with real Jre's Entry.toString(): issue 3422
  public void testTreeMapEntryToString() {
    Map<String, String> treeMap = new TreeMap<String, String>();
    treeMap.put("bar", "barValue");

    assertEquals("bar=barValue",
        treeMap.entrySet().iterator().next().toString());
  }
  
  @Override
  protected Object getConflictingKey() {
    return new Integer(1);
  }

  @Override
  protected Object getConflictingValue() {
    return new Long(42);
  }

  @Override
  String getGreaterThanMaximumKey() {
    return "z";
  }

  @Override
  String[] getKeys() {
    return convertToStringArray(getSampleKeys());
  }

  @Override
  String[] getKeys2() {
    return convertToStringArray(getOtherKeys());
  }

  @Override
  String getLessThanMinimumKey() {
    return "a";
  }

  @Override
  String[] getValues() {
    return convertToStringArray(getSampleValues());
  }

  @Override
  String[] getValues2() {
    return convertToStringArray(getOtherValues());
  }

  private String[] convertToStringArray(Object[] objArray) {
    String[] strArray = new String[objArray.length];
    System.arraycopy(objArray, 0, strArray, 0, objArray.length);
    return strArray;
  }

  private SortedMap<String, String> createKnownKeysMap() {
    SortedMap<String, String> sortedMap = createSortedMap();
    sortedMap.put("dd", "dval");
    sortedMap.put("aa", "aval");
    sortedMap.put("cc", "cval");
    sortedMap.put("bb", "bval");
    return sortedMap;
  }
}
