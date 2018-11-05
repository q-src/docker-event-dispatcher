package com.github.qsrc;

import org.apache.log4j.lf5.LogLevel;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {

    private Logger logger;

    private LogLevel level;

    StringBuilder log;

    @Override
    public void write(int i) throws IOException {
    }

}
