package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.util.concurrent.Callable;

public interface ProcessorSupplier<R> {
  public Callable<R> getProcessor(Integer etext_no, String language, File file);
}
