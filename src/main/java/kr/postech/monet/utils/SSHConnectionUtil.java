package kr.postech.monet.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.VMBean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by woojoong on 2017-05-18.
 */
public class SSHConnectionUtil {

    private final int SSH_TIMEOUT = 10000;

    public SSHConnectionUtil() {

    }

    /**
     * To send a command message to a single PM through SSH
     * @param targetPM
     * @param cmd
     * @param bufSize
     * @return
     */
    public String sendCmdToSinglePM (PMBean targetPM, String cmd, int bufSize) {

        String resultCommand = null;
        StringBuffer sb = new StringBuffer();

        Session session = null;

        // Establish SSH connection
        try {
            session = new JSch().getSession(targetPM.getID(),
                    targetPM.getAccessIPAddress(),
                    Integer.parseInt(targetPM.getAccessSSHPort()));
            session.setTimeout(SSH_TIMEOUT);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(targetPM.getPW());
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println("SSH connection error (session): targetPM - " + targetPM.getAccessIPAddress() + " Port - " + targetPM.getAccessSSHPort());
        }

        ChannelExec channel = null;
        // Send command
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd);
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println("SSH connection error (channel): targetPM - " + targetPM.getAccessIPAddress() + " Port - " + targetPM.getAccessSSHPort());
        }

        // Get results
        try {
            InputStreamReader ir = new InputStreamReader(channel.getInputStream());
            char[] tmpBuf = new char[bufSize];
            while (ir.read(tmpBuf) != -1) {
                sb.append(tmpBuf);
            }
            resultCommand = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SSH connection error (command): targetPM - " + targetPM.getAccessIPAddress() + " Port - " + targetPM.getAccessSSHPort());
        }

        // Disconnect channel and session
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }

        return resultCommand;
    }

    /**
     * To send a command message to a single VM through SSH
     * @param targetVM
     * @param cmd
     * @param bufSize
     * @return
     */
    public String sendCmdToSingleVM (VMBean targetVM, String cmd, int bufSize) {
        String resultCommand = null;
        StringBuffer sb = new StringBuffer();

        Session session = null;

        // Establish SSH connection
        try {
            session = new JSch().getSession(targetVM.getID(),
                    targetVM.getAccessIPAddress(),
                    Integer.parseInt(targetVM.getAccessSSHPort()));
            session.setTimeout(SSH_TIMEOUT);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(targetVM.getPW());
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println("SSH connection error (session): targetVM - " + targetVM.getAccessIPAddress() + " Port - " + targetVM.getAccessSSHPort());
        }

        ChannelExec channel = null;
        // Send command
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd);
            channel.connect();
        } catch (JSchException e) {
            System.out.println("SSH connection error (channel): targetVM - " + targetVM.getAccessIPAddress() + " Port - " + targetVM.getAccessSSHPort());
            e.printStackTrace();
        }

        // Get results
        try {
            InputStreamReader ir = new InputStreamReader(channel.getInputStream());
            char[] tmpBuf = new char[bufSize];
            while (ir.read(tmpBuf) != -1) {
                sb.append(tmpBuf);
            }
            resultCommand = sb.toString();
        } catch (IOException e) {
            System.out.println("SSH connection error (command): targetVM - " + targetVM.getAccessIPAddress() + " Port - " + targetVM.getAccessSSHPort());
            e.printStackTrace();
        }

        // Disconnect channel and session
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }

        return resultCommand;
    }

}
