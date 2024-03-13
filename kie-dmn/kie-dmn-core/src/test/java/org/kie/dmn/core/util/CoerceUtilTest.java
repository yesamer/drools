package org.kie.dmn.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.core.impl.SimpleTypeImpl;
import org.kie.dmn.feel.lang.types.BuiltInType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoerceUtilTest {

    @Test
    public void coerceValueCollectionToArrayConverted() {
        Object item = "TESTED_OBJECT";
        Object value = Collections.singleton(item);
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "string",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.STRING);
        Object retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(item, retrieved);
    }

    @Test
    public void coerceValueCollectionToArrayNotConverted() {
        Object item = "TESTED_OBJECT";
        Object value = Collections.singleton(item);
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "string",
                                                  null,
                                                  true,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.STRING);
        Object retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(value, retrieved);

        value = "TESTED_OBJECT";
        requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "string",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.STRING);
        retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(value, retrieved);

        requiredType = null;
        retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertEquals(value, retrieved);

        value = null;
        requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                         "string",
                                                         null,
                                                         false,
                                                         null,
                                                         null,
                                                         null,
                                                         BuiltInType.STRING);
        retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertEquals(value, retrieved);

    }

    @Test
    public void coerceValueDateToDateTimeConverted() {
        Object value = LocalDate.now();
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "date and time",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.DATE_TIME);
        Object retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof ZonedDateTime);
        ZonedDateTime zdtRetrieved = (ZonedDateTime)retrieved;
        assertEquals(value, zdtRetrieved.toLocalDate());
        assertEquals(ZoneOffset.UTC, zdtRetrieved.getOffset());
        assertEquals(0, zdtRetrieved.getHour());
        assertEquals(0, zdtRetrieved.getMinute());
        assertEquals(0, zdtRetrieved.getSecond());
    }

    @Test
    public void coerceValueDateToDateTimeNotConverted() {
        Object value = "TEST_OBJECT";
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "date and time",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.DATE_TIME);
        Object retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(value, retrieved);
        value = LocalDate.now();
        requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "date",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.DATE);
        retrieved = CoerceUtil.coerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(value, retrieved);
    }

    @Test
    public void actualCoerceValueCollectionToArray() {
        Object item = "TESTED_OBJECT";
        Object value = Collections.singleton(item);
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "string",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.STRING);
        Object retrieved = CoerceUtil.actualCoerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(item, retrieved);
    }

    @Test
    public void actualCoerceValueDateToDateTime() {
        Object value = LocalDate.now();
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "date and time",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.DATE_TIME);
        Object retrieved = CoerceUtil.actualCoerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof ZonedDateTime);
        ZonedDateTime zdtRetrieved = (ZonedDateTime)retrieved;
        assertEquals(value, zdtRetrieved.toLocalDate());
        assertEquals(ZoneOffset.UTC, zdtRetrieved.getOffset());
        assertEquals(0, zdtRetrieved.getHour());
        assertEquals(0, zdtRetrieved.getMinute());
        assertEquals(0, zdtRetrieved.getSecond());
    }

    @Test
    public void actualCoerceValueNotConverted() {
        Object value = BigDecimal.valueOf(1L);
        DMNType requiredType = new SimpleTypeImpl("http://www.omg.org/spec/DMN/20180521/FEEL/",
                                                  "number",
                                                  null,
                                                  false,
                                                  null,
                                                  null,
                                                  null,
                                                  BuiltInType.NUMBER);
        Object retrieved = CoerceUtil.actualCoerceValue(requiredType, value);
        assertNotNull(retrieved);
        assertEquals(value, retrieved);
    }

    @Test
    public void coerceNumericValuesToBigDecimalSimpleNumericData() {
        Map<String, Object> rootValue = new HashMap<>();
        rootValue.put("value", BigInteger.valueOf(1));
        Map<String, Object> returnedValue = (Map<String, Object>) CoerceUtil.coerceNumericValuesToBigDecimal(rootValue);

        assertEquals(returnedValue.entrySet().size(), 1);
        assertTrue(returnedValue.get("value") instanceof BigDecimal);
        assertEquals(BigDecimal.valueOf(1), returnedValue.get("value"));
    }

    @Test
    public void coerceNumericValuesToBigDecimalComplexData() {
        Map<String, Object> bookValue = new HashMap<>();
        bookValue.put("pages", 103);
        bookValue.put("chapters", List.of(List.of(45, 46, 47), List.of(54, 55)));
        bookValue.put("sections", new Long[][]{ {1L, 2L}, {4L, 5L, 6L} });
        bookValue.put("price", 3.5);
        Map<String, Object> personValue = new HashMap<>();
        personValue.put("name", "John");
        personValue.put("age", 10);
        personValue.put("points", List.of(10, 20, 30));
        personValue.put("Book", bookValue);

        Map<String, Object> rootValue = new HashMap<>();
        rootValue.put("Person", personValue);

        Map<String, Object> returnedValue = (Map<String, Object>) CoerceUtil.coerceNumericValuesToBigDecimal(rootValue);
        Map<String, Object> returnedPerson = (Map<String, Object>) returnedValue.get("Person");
        Map<String, Object> returnedBook = (Map<String, Object>) returnedPerson.get("Book");

        assertEquals(returnedValue.entrySet().size(), 1);
        assertEquals(returnedPerson.entrySet().size(), 4);
        assertTrue(returnedPerson.get("age") instanceof BigDecimal);
        assertEquals(returnedPerson.get("age"), new BigDecimal(10));
        assertEquals(returnedPerson.get("name"), "John");
        List returnedPersonPoints = (List) returnedPerson.get("points");
        assertEquals(returnedPersonPoints.size(), 3);
        assertTrue(returnedPersonPoints.get(0) instanceof BigDecimal);
        assertEquals(returnedPersonPoints.get(0), new BigDecimal(10));
        assertTrue(returnedPersonPoints.get(1) instanceof BigDecimal);
        assertEquals(returnedPersonPoints.get(1), new BigDecimal(20));
        assertTrue(returnedPersonPoints.get(2) instanceof BigDecimal);
        assertEquals(returnedPersonPoints.get(2), new BigDecimal(30));
        assertTrue(returnedBook.get("pages") instanceof BigDecimal);
        assertEquals(returnedBook.get("pages"), new BigDecimal(103));
        assertTrue(returnedBook.get("price") instanceof BigDecimal);
        assertEquals(returnedBook.get("price"), new BigDecimal(3.5));
        List returnedBookChapters = (List) returnedBook.get("chapters");
        assertEquals(2, returnedBookChapters.size());
        assertEquals(((List) returnedBookChapters.get(0)).size(), 3);
        assertEquals(((List) returnedBookChapters.get(0)).get(0), new BigDecimal(45));
        assertEquals(((List) returnedBookChapters.get(0)).get(1), new BigDecimal(46));
        assertEquals(((List) returnedBookChapters.get(0)).get(2), new BigDecimal(47));
        assertEquals(((List) returnedBookChapters.get(1)).size(), 2);
        assertEquals(((List) returnedBookChapters.get(1)).get(0), new BigDecimal(54));
        assertEquals(((List) returnedBookChapters.get(1)).get(1), new BigDecimal(55));
        Object[] returnedBookSections = (Object[]) returnedBook.get("sections");
        assertEquals(returnedBookSections.length, 2);
        assertEquals(((Object[]) returnedBookSections[0]).length, 2);
        assertEquals(((Object[]) returnedBookSections[0])[0], new BigDecimal(1));
        assertEquals(((Object[]) returnedBookSections[0])[1], new BigDecimal(2));
        assertEquals(((Object[]) returnedBookSections[1]).length, 3);
        assertEquals(((Object[]) returnedBookSections[1])[0], new BigDecimal(4));
        assertEquals(((Object[]) returnedBookSections[1])[1], new BigDecimal(5));
        assertEquals(((Object[]) returnedBookSections[1])[2], new BigDecimal(6));
    }

}