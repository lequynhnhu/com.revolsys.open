/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.orm.jpa.dao;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.support.JpaDaoSupport;

import com.revolsys.orm.core.NamedQueryParameter;

public class JpaDaoHandler extends JpaDaoSupport implements InvocationHandler {
  /** The class definition of the DataAcessObject interface. */
  private Class<?> daoInterface;

  /** The class definition of the entities persisted by this Data Access Object. */
  private Class<?> objectClass;

  /** The class name of the entities persisted by this Data Access Object. */
  private String objectClassName;

  public JpaDaoHandler(final Class<?> daoInterface, final Class<?> objectClass) {
    this.daoInterface = daoInterface;
    this.objectClass = objectClass;
    this.objectClassName = objectClass.getName();
  }

  /**
   * Clear all objects loaded from persistent storage from the cache. After
   * invoking this method the in memory Java objects will be disconnected from
   * the persistent storage and any changes to them will not be saved.
   */
  public void clearCache() {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager entityManager)
        throws PersistenceException {
        entityManager.clear();
        return null;
      }
    });
  }

  public Object evict(final Object object) {

    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager entityManager)
        throws PersistenceException {
        Object delegate = entityManager.getDelegate();
        if (delegate instanceof Session) {
          Session session = (Session)delegate;
          session.evict(object);
        }
        return null;
      };
    });
  }

  @SuppressWarnings("unchecked")
  public List<Object> find(final String queryName, final Object... args) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.findByNamedQuery(
      objectClass.getName() + "." + queryName, args);
  }

  @SuppressWarnings("unchecked")
  public Iterator<Object> iterate(final Method method, final String queryName,
    final Object... args) {
    final String fullQueryName = objectClass.getName() + "." + queryName;
    JpaTemplate jpaTemplate = getJpaTemplate();
    return (Iterator<Object>)jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager entityManager)
        throws PersistenceException {
        Object delegate = entityManager.getDelegate();
        if (delegate instanceof Session) {
          Session session = (Session)delegate;
          org.hibernate.Query query = session.getNamedQuery(fullQueryName);
          Annotation[][] parameterAnnotations = method.getParameterAnnotations();
          int paramIndex = 0;
          for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            boolean found = false;
            for (Annotation annotation : parameterAnnotations[i]) {
              if (annotation.annotationType().equals(NamedQueryParameter.class)) {
                String parameterName = ((NamedQueryParameter)annotation).value();
                if (value instanceof Collection) {
                  query.setParameterList(parameterName, (Collection)value);
                } else if (value instanceof Object[]) {
                  query.setParameterList(parameterName, (Object[])value);
                } else {
                  query.setParameter(parameterName, value);
                }
                found = true;
              }

            }

            if (!found) {
              query.setParameter(paramIndex++, value);
            }
          }
          return query.iterate();
        } else {
          Query queryObject = entityManager.createNamedQuery(queryName);
          for (int i = 0; i < args.length; i++) {
            queryObject.setParameter(i + 1, args[i]);
          }
          return queryObject.getResultList().iterator();
        }
      }
    });
  }

  /**
   * Flush all changes to the persistent storage.
   */
  public void flush() {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.flush();
  }

  public Object get(final String queryName, final Object[] args) {

    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager em) throws PersistenceException {
        Query query = em.createNamedQuery(objectClassName + "." + queryName);
        for (int i = 0; i < args.length; i++) {
          Object arg = args[i];
          query.setParameter(i + 1, arg);
        }
        try {
          return query.getSingleResult();
        } catch (NoResultException e) {
          return null;
        }
      };
    });
  }

  public String getQueryName(final String methodName) {
    String queryName = methodName.replaceFirst("\\A(get|page|find|iterate)", "");
    return objectClassName + "." + queryName;
  }

  @SuppressWarnings("unchecked")
  public Object invoke(final Object proxy, final Method method,
    final Object[] args) throws Throwable {
    String methodName = method.getName();
    try {
      Class<?>[] paramTypes = method.getParameterTypes();
      Method localMethod = getClass().getMethod(methodName, paramTypes);
      return localMethod.invoke(this, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (SecurityException e) {
      throw e;
    } catch (NoSuchMethodException e) {
      if (methodName.equals("removeAll")) {
        return removeAll((Collection<Object>)args[0]);
      } else if (methodName.startsWith("remove")) {
        if (args[0] instanceof Long) {
          return remove((Long)args[0]);
        } else {
          return remove(args[0]);
        }
      } else if (methodName.startsWith("find")) {
        return find(methodName, args);
      } else if (methodName.startsWith("iterate")) {
        return iterate(method, methodName.substring(7), args);
      } else if (methodName.startsWith("get")) {
        return get(methodName.substring(3), args);
      } else if (methodName.startsWith("persist")) {
        return persist(args[0]);
      } else if (methodName.startsWith("load")) {
        return load(args[0]);
      } else if (methodName.startsWith("delete")) {
        return update(method, methodName, args);
      } else if (methodName.startsWith("update")) {
        return update(method, methodName.substring(6), args);
      } else {
        throw new IllegalArgumentException("Method " + methodName
          + " does not exist");
      }
    }

  }

  /**
   * Load the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return The object.
   */
  public Object load(final Object id) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    Object object = jpaTemplate.find(objectClass, (Serializable)id);
    return object;
  }

  /**
   * Load the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return The object.
   * @throws ObjectNotFoundException If the object was not found.
   */
  public Object loadAndLock(final Object id) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    Object object = jpaTemplate.find(objectClass, (Serializable)id);
    lockAndRefresh(object);
    return object;
  }

  /**
   * Create a lock on the object so that no other transactions can modifiy the
   * object.
   * 
   * @param object The object to lock.
   * @return null.
   * @throws ObjectNotFoundException If the object to be locked could not be
   *           found.
   */
  public Object lock(final Object object) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager entityManager)
        throws PersistenceException {
        entityManager.lock(object, LockModeType.WRITE);
        return null;
      }
    });

  }

  /**
   * Create a lock on the object so that no other transactions can modifiy the
   * object.
   * 
   * @param object The object to lock.
   * @return null.
   * @throws ObjectNotFoundException If the object to be locked could not be
   *           found.
   */
  public Object lockAndRefresh(final Object object) {
    lock(object);
    refresh(object);
    return null;
  }

  public Object merge(final Object object) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.merge(object);
    return null;
  }

  /**
   * Insert a new object to the persistent storage and get the ID for the
   * object.
   * 
   * @param object The object to insert.
   * @return The ID of the object in the persistent storage.
   */
  public Object persist(final Object object) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.persist(object);
    return null;
  }

  /**
   * Refresh the values of the object from the database.
   * 
   * @param object The object to refresh.
   * @return null.
   */
  public Object refresh(final Object object) {
    getJpaTemplate().refresh(object);
    return null;
  }

  /**
   * Delete the object with the ID from persistent storage.
   * 
   * @param id The ID of the object to delete.
   * @return null.
   * @throws ObjectNotFoundException If the object was not found.
   */
  public Object remove(final Long id) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.remove(load(id));
    return null;

  }

  /**
   * Delete the object from persistent storage.
   * 
   * @param object The object to delete.
   * @return null.
   */
  public Object remove(final Object object) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    jpaTemplate.remove(object);
    return null;
  }

  /**
   * Delete all objects in the collection from persistent storage.
   * 
   * @param objects The list of objects to delete.
   * @return null.
   */
  public Object removeAll(final Collection<Object> objects) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    for (Object object : objects) {
      jpaTemplate.remove(object);
    }
    return null;
  }

  /**
   * Save the values of an updated object in the persistent storage.
   * 
   * @param object The object to update.
   * @return null.
   */
  public Object update(final Object object) {
    getJpaTemplate().merge(object);
    return null;
  }

  public Object update(final Method method, final String queryName,
    final Object[] args) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager entityManager)
        throws PersistenceException {
        String fullQueryName = objectClassName + "." + queryName;
        Object delegate = entityManager.getDelegate();
        if (delegate instanceof Session) {
          Session session = (Session)delegate;
          org.hibernate.Query query = session.getNamedQuery(fullQueryName);
          Annotation[][] parameterAnnotations = method.getParameterAnnotations();
          int paramIndex = 0;
          for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            boolean found = false;
            for (Annotation annotation : parameterAnnotations[i]) {
              if (annotation.annotationType().equals(NamedQueryParameter.class)) {
                String parameterName = ((NamedQueryParameter)annotation).value();
                if (value instanceof Collection) {
                  query.setParameterList(parameterName, (Collection)value);
                } else if (value instanceof Object[]) {
                  query.setParameterList(parameterName, (Object[])value);
                } else {
                  query.setParameter(parameterName, value);
                }
                found = true;
              }

            }

            if (!found) {
              query.setParameter(paramIndex++, value);
            }
          }
          return (Integer)query.executeUpdate();
        } else {
          Query query = entityManager.createNamedQuery(fullQueryName);
          for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            query.setParameter(i + 1, arg);
          }
          try {
            return (Integer)query.executeUpdate();
          } catch (NoResultException e) {
            return null;
          }
        }

      };
    });
  }

  public Object delete(final String queryName, final Object[] args) {
    JpaTemplate jpaTemplate = getJpaTemplate();
    return jpaTemplate.execute(new JpaCallback() {
      public Object doInJpa(EntityManager em) throws PersistenceException {
        Query query = em.createNamedQuery(objectClassName + "." + queryName);
        for (int i = 0; i < args.length; i++) {
          Object arg = args[i];
          query.setParameter(i + 1, arg);
        }
        try {
          return (Integer)query.executeUpdate();
        } catch (NoResultException e) {
          return null;
        }
      };
    });
  }
}