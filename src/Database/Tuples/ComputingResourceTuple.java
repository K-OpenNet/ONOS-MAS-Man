package Database.Tuples;

import java.util.ArrayList;

public class ComputingResourceTuple extends AbstractTuple implements Tuple {

    private ArrayList<Double> cpuUsageUser;
    private ArrayList<Double> cpuUsageKernel;
    private ArrayList<Integer> ramUsage;
    private ArrayList<Integer> netRx;
    private ArrayList<Integer> netTx;

    public ComputingResourceTuple() {
        tupleName = tupleType.COMPUTINGRESOURCETUPLE;
        cpuUsageUser = new ArrayList<>();
        cpuUsageKernel = new ArrayList<>();
        ramUsage = new ArrayList<>();
        netRx = new ArrayList<>();
        netTx = new ArrayList<>();
    }

    public double avgCpuUsageUser() {
        double sumCpuUsageUser = 0.0;

        for (int index = 0; index < cpuUsageUser.size(); index++) {
            sumCpuUsageUser += cpuUsageUser.get(index);
        }

        return sumCpuUsageUser/((double) cpuUsageUser.size());
    }

    public double avgCpuUsageKernel() {
        double sumCpuUsageKernel = 0.0;

        for (int index = 0; index < cpuUsageKernel.size(); index++) {
            sumCpuUsageKernel += cpuUsageKernel.get(index);
        }

        return sumCpuUsageKernel/((double) cpuUsageKernel.size());
    }

    public double avgCpuUsage() {
        return avgCpuUsageUser() + avgCpuUsageKernel();
    }

    public double avgRamUsage() {
        int sumRamUsage = 0;

        for (int index = 0; index < ramUsage.size(); index++) {
            sumRamUsage += ramUsage.get(index);
        }

        return ((double) sumRamUsage)/((double) ramUsage.size());
    }

    public double avgNetRx() {
        int sumNetRx = 0;

        for (int index = 0; index < netRx.size(); index++) {
            sumNetRx += netRx.get(index);
        }

        return ((double) sumNetRx)/((double) netRx.size());
    }

    public double avgNetTx() {
        int sumNetTx = 0;

        for (int index = 0; index < netTx.size(); index++) {
            sumNetTx += netTx.get(index);
        }

        return ((double) sumNetTx)/((double) netTx.size());
    }

    public double avgNet() {
        return avgNetRx() + avgNetTx();
    }

    public double avgNetBandwidth() {
        return avgNetRx() + avgNetTx();
    }

    public ArrayList<Double> getCpuUsageUser() {
        return cpuUsageUser;
    }

    public void setCpuUsageUser(ArrayList<Double> cpuUsageUser) {
        this.cpuUsageUser = cpuUsageUser;
    }

    public ArrayList<Double> getCpuUsageKernel() {
        return cpuUsageKernel;
    }

    public void setCpuUsageKernel(ArrayList<Double> cpuUsageKernel) {
        this.cpuUsageKernel = cpuUsageKernel;
    }

    public ArrayList<Integer> getRamUsage() {
        return ramUsage;
    }

    public void setRamUsage(ArrayList<Integer> ramUsage) {
        this.ramUsage = ramUsage;
    }

    public ArrayList<Integer> getNetRx() {
        return netRx;
    }

    public void setNetRx(ArrayList<Integer> netRx) {
        this.netRx = netRx;
    }

    public ArrayList<Integer> getNetTx() {
        return netTx;
    }

    public void setNetTx(ArrayList<Integer> netTx) {
        this.netTx = netTx;
    }
}
