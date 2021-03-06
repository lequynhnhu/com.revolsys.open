package com.revolsys.swing.parallel;

import java.awt.Image;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.revolsys.swing.listener.MacApplicationListenerHandler;
import com.revolsys.swing.logging.ListLog4jAppender;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class BaseMain implements UncaughtExceptionHandler {

  public static void run(final Class<? extends BaseMain> mainClass,
    final String[] args) {
    try {
      final BaseMain main = mainClass.newInstance();
      main.processArguments(args);
      main.run();
    } catch (final Throwable e) {
      ExceptionUtil.log(mainClass, e);
    }
  }

  protected static void setMacDockIcon(final Image image) {
    try {
      final Class<?> clazz = Class.forName("com.apple.eawt.Application");
      final Method appMethod = clazz.getMethod("getApplication");
      final Object application = appMethod.invoke(clazz);
      if (image != null) {
        MethodUtils.invokeMethod(application, "setDockIconImage", image);
      }
      final Class<?> quitStrategyClass = Class.forName("com.apple.eawt.QuitStrategy");
      final Object closeAllWindows = quitStrategyClass.getField(
          "CLOSE_ALL_WINDOWS").get(quitStrategyClass);
      MethodUtils.invokeExactMethod(application, "setQuitStrategy",
        closeAllWindows);
      MacApplicationListenerHandler.init(application);
    } catch (final ClassNotFoundException t) {
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private final String name;

  private String lookAndFeelName;

  public BaseMain(final String name) {
    this.name = name;
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  public void doPreRun() throws Throwable {

  }

  public void doRun() throws Throwable {
    boolean lookSet = false;
    if (Property.hasValue(this.lookAndFeelName)) {
      final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
      for (final LookAndFeelInfo lookAndFeelInfo : installedLookAndFeels) {
        final String name = lookAndFeelInfo.getName();
        if (this.lookAndFeelName.equals(name)) {
          try {
            final String className = lookAndFeelInfo.getClassName();
            UIManager.setLookAndFeel(className);
            lookSet = true;
          } catch (final Throwable e) {
          }
        }
      }
    }
    if (!lookSet) {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    ToolTipManager.sharedInstance().setInitialDelay(100);
  }

  public String getLookAndFeelName() {
    return this.lookAndFeelName;
  }

  public void processArguments(final String[] args) {
  }

  public void run() {
    try {
      doPreRun();
      Invoke.later(this, "doRun");
    } catch (final Throwable e) {
      final Logger logger = Logger.getLogger(getClass());
      final LoggingEvent event = new LoggingEvent(logger.getClass().getName(),
        logger, Level.ERROR, "Unable to start application", e);

      LoggingEventPanel.showDialog(null, event);
      ExceptionUtil.log(getClass(), "Unable to start application " + this.name,
        e);
    }
  }

  public void setLookAndFeelName(final String lookAndFeelName) {
    this.lookAndFeelName = lookAndFeelName;
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    final Class<? extends BaseMain> logClass = getClass();
    String message = e.getMessage();
    if (!Property.hasValue(message)) {
      if (e instanceof NullPointerException) {
        message = "Null pointer";
      } else {
        message = "Unknown error";
      }
    }
    ExceptionUtil.log(logClass, message, e);
    @SuppressWarnings("unchecked")
    final Enumeration<Appender> allAppenders = Logger.getRootLogger()
    .getAllAppenders();
    while (allAppenders.hasMoreElements()) {
      final Appender appender = allAppenders.nextElement();
      if (appender instanceof ListLog4jAppender) {
        return;
      }
    }
    final Logger logger = Logger.getLogger(logClass);
    final LoggingEvent event = new LoggingEvent(logger.getClass().getName(),
      logger, Level.ERROR, message, e);

    LoggingEventPanel.showDialog(null, event);

  }
}
