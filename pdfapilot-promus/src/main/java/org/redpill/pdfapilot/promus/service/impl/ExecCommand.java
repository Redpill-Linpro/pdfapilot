package org.redpill.pdfapilot.promus.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ExecCommand {

  private Semaphore _outputSem;
  private List<String> _output = new ArrayList<String>();
  private Semaphore _errorSem;
  private List<String> _error = new ArrayList<String>();
  private Process _process;

  private class InputWriter extends Thread {

    private String _input;

    public InputWriter(String input) {
      _input = input;
    }

    public void run() {
      PrintWriter pw = new PrintWriter(_process.getOutputStream());

      pw.println(_input);

      pw.flush();
    }
  }

  private class OutputReader extends Thread {
    public OutputReader() {
      try {
        _outputSem = new Semaphore(1);

        _outputSem.acquire();
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }

    public void run() {
      try {
        BufferedReader isr = new BufferedReader(new InputStreamReader(_process.getInputStream()));

        String buff = new String();

        while ((buff = isr.readLine()) != null) {
          _output.add(buff);
        }

        _outputSem.release();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  private class ErrorReader extends Thread {
    public ErrorReader() {
      try {
        _errorSem = new Semaphore(1);

        _errorSem.acquire();
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }

    public void run() {
      try {
        BufferedReader isr = new BufferedReader(new InputStreamReader(_process.getErrorStream()));

        String buff = new String();

        while ((buff = isr.readLine()) != null) {
          _error.add(buff);
        }

        _errorSem.release();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  public ExecCommand(String[] command, String input) {
    try {
      _process = Runtime.getRuntime().exec(command);

      new InputWriter(input).start();

      new OutputReader().start();

      new ErrorReader().start();

      _process.waitFor();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public ExecCommand(String[] command) {
    try {
      _process = Runtime.getRuntime().exec(command);

      new OutputReader().start();

      new ErrorReader().start();

      _process.waitFor();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<String> getOutput() {
    try {
      _outputSem.acquire();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
    
    List<String> value = _output;

    _outputSem.release();

    return value;
  }

  public List<String> getError() {
    try {
      _errorSem.acquire();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }

    List<String> value = _error;

    _errorSem.release();

    return value;
  }

  public int getExitValue() {
    return _process.exitValue();
  }

}