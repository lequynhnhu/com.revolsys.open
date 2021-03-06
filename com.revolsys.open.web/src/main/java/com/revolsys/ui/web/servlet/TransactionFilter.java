package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.ui.web.utils.HttpSavedRequestAndResponse;
import com.revolsys.util.WrappedException;

public class TransactionFilter extends GenericFilterBean {
  private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

  private WebApplicationContext applicationContext;

  @Override
  public void destroy() {
    this.applicationContext = null;
  }

  @Override
  public void doFilter(final ServletRequest servletRequest,
    final ServletResponse servletResponse, final FilterChain filterChain)
        throws IOException, ServletException {
    final AbstractPlatformTransactionManager transactionManager = (AbstractPlatformTransactionManager)this.applicationContext.getBean("transactionManager");
    try (
        HttpSavedRequestAndResponse saved = new HttpSavedRequestAndResponse(
          servletRequest, servletResponse);
        Transaction transaction = new Transaction(transactionManager,
          Propagation.REQUIRED);) {
      try {
        filterChain.doFilter(servletRequest, servletResponse);
      } catch (final Throwable e) {
        transaction.setRollbackOnly();
        throw e;
      }
    } catch (IOException | ServletException | Error | RuntimeException e) {
      HttpServletLogUtil.logRequestException(log, e);
      throw e;
    } catch (final Throwable e) {
      HttpServletLogUtil.logRequestException(log, e);
      throw new WrappedException(e);
    }
  }

  @Override
  protected void initFilterBean() throws ServletException {
    final ServletContext servletContext = getServletContext();
    this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
  }
}
