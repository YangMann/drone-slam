/*
 *
  Copyright (c) <2011>, <Shigeo Yoshida>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
The names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package drone_slam.base.configuration;

import drone_slam.base.command.CommandManager;
import drone_slam.base.command.ControlCommand;
import drone_slam.base.command.ControlMode;
import drone_slam.base.exception.ConfigurationException;
import drone_slam.base.exception.IExceptionListener;
import drone_slam.base.manager.AbstractTCPManager;
import drone_slam.base.utils.ARDroneUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

// TODO consider to connect to the control port permanently
public class ConfigurationManager extends AbstractTCPManager {
    private IExceptionListener excListener;

    private CommandManager manager = null;

    public ConfigurationManager(InetAddress inetaddr, CommandManager manager, IExceptionListener excListener) {
        super(inetaddr);
        this.manager = manager;
        this.excListener = excListener;
    }

    public void run() {
        try {
            connect(ARDroneUtils.CONTROL_PORT);
        } catch (Exception exc) {
            exc.printStackTrace();
            excListener.exeptionOccurred(new ConfigurationException(exc));
        }
    }

    /**
     * Note: not thread-safe!
     */
    private String getControlCommandResult(ControlMode p1, int p2, final ConfigurationListener listener) {
        manager.setCommand(new ControlCommand(p1, p2));

        Thread t = new Thread() {

            public void run() {
                try {

                    InputStream inputStream = getInputStream();
                    // TODO better getInputStream throw IOException to fail
                    if (inputStream != null) {
                        byte[] buf = new byte[1024];
                        int n = 0;
                        StringBuilder builder = new StringBuilder();
                        try {
                            while ((n = inputStream.read(buf)) != -1) {
                                // output: multiple rows of "Parameter = value"
                                builder.append(new String(buf, 0, n, "ASCII"));
                            }
                        } catch (SocketTimeoutException e) {
                            // happens if the last byte happens to coincide with the end of the buffer
                        }
                        String s = builder.toString();
                        if (listener != null) {
                            listener.result(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        return "";

    }

    public String getCustomCofigurationIds(ConfigurationListener listener) {
        String s = getControlCommandResult(ControlMode.CUSTOM_CFG_GET, 0, listener);
        return s;
    }

    public String getPreviousRunLogs(ConfigurationListener listener) {
        String s = getControlCommandResult(ControlMode.LOGS_GET, 0, listener);
        return s;
    }

    public String getConfiguration(ConfigurationListener listener) {
        String s = getControlCommandResult(ControlMode.CFG_GET, 0, listener);
        return s;
    }

}
