package com.j256.ormlite.table;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

/**
 * Interface that allows you to inject a factory class that creates objects of this class. You sert it on the Dao using:
 * {@link com.j256.ormlite.dao.Dao#setObjectFactory(ObjectFactory)}.
 *
 * @author graywatson
 */
public interface ObjectFactory<T> {

    /**
     * Construct and return an object of a certain class.
     *
     * @throws SQLException if there was a problem creating the object.
     */
    public T createObject(Constructor<T> construcor, Class<T> dataClass) throws SQLException;
}
