package Utils.Connection;

import Beans.Bean;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class SSHConnection extends AbstractConnection implements Connection {
    public SSHConnection() {
    }

    @Override
    public String sendCommandToUser(Bean targetMachine, String cmd) {
        String results = null;
        StringBuffer sb = new StringBuffer();

        ChannelExec channel = null;
        try {
            channel = (ChannelExec) targetMachine.getUserSession().openChannel("exec");
            channel.setCommand(cmd);
            channel.connect();

            InputStream is = channel.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String tmpLine;
            results = br.readLine() + "\n";
            while ((tmpLine = br.readLine()) != null) {
                results = results + tmpLine + "\n";
            }

            br.close();
            is.close();

            channel.disconnect();

        } catch (Exception e) {
            System.out.println("SSH Command does not successfully send toward target machine in user session");
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public String sendCommandToRoot(Bean targetMachine, String cmd) {
        String results = null;
        StringBuffer sb = new StringBuffer();

        ChannelExec channel = null;
        try {

            System.out.println(targetMachine.getBeanKey() + ": 1");

            channel = (ChannelExec) targetMachine.getRootSession().openChannel("exec");
            channel.setCommand(cmd);
            channel.connect();

            System.out.println(targetMachine.getBeanKey() + ": 2");

            InputStream is = channel.getInputStream();
            byte[] buf = new byte[2048];
            int index;

            System.out.println(targetMachine.getBeanKey() + ": 3");

            while((index = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, index));
            }

            System.out.println(targetMachine.getBeanKey() + ": 4");

            is.close();

            channel.disconnect();

            System.out.println(targetMachine.getBeanKey() + ": 5");

        } catch (Exception e) {
            System.out.println("SSH Command does not successfully send toward target machine in Root session");
            e.printStackTrace();
        }

        return results;
    }

    public void assignUserSession (Bean targetMachine) {
        if (!isMachine(targetMachine)) {
            return;
        }

        Session session = null;

        try {
            session = new JSch().getSession(targetMachine.getSshId(), targetMachine.getIpAddr(), Integer.valueOf(targetMachine.getSshPort()));
            Properties conf = new Properties();
            conf.put("StrictHostKeyChecking", "no");
            session.setConfig(conf);
            session.setPassword(targetMachine.getSshPw());
            session.connect();
        } catch (JSchException e) {
            System.out.println("SSH session has not been made successfully");
            e.printStackTrace();
        }

        targetMachine.setUserSession(session);
    }

    public void assignRootSession (Bean targetMachine) {
        if (!isMachine(targetMachine)) {
            return;
        }

        Session session = null;

        try {
            session = new JSch().getSession(targetMachine.getSshRootId(), targetMachine.getIpAddr(), Integer.valueOf(targetMachine.getSshPort()));
            Properties conf = new Properties();
            conf.put("StrictHostKeyChecking", "no");
            session.setConfig(conf);
            session.setPassword(targetMachine.getSshRootPw());
            session.connect();
        } catch (JSchException e) {
            System.out.println("SSH session has not been made successfully");
            e.printStackTrace();
        }

        targetMachine.setRootSession(session);
    }

    public void removeUserSession (Bean targetMachine) {
        if (!isMachine(targetMachine)) {
            return;
        }

        targetMachine.getUserSession().disconnect();
    }

    public void removeRootSession (Bean targetMachine) {
        if (!isMachine(targetMachine)) {
            return;
        }

        targetMachine.getRootSession().disconnect();
    }

    public boolean isMachine (Bean targetMachine) {
        switch (targetMachine.getBeanName()) {
            case CONTROLLER:
                return true;
            case PM:
                return true;
            default:
                return false;
        }
    }

}
