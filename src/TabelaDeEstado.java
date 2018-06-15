import java.net.InetAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class Estado implements Comparable<Estado>, Comparator<Estado> {
    long ram;
    double cpu;
    float rtt;
    InetAddress add;
    int port;
    float bandwidth;
    public Estado(long ram, double cpu, float rtt, InetAddress ip, int port) {
        this.ram = ram;
        this.cpu = cpu;
        this.rtt = rtt;
        this.add = ip;
        this.port = port;
        this.bandwidth = 0;
    }

    public static Estado melhorEst(Estado e1, Estado e2){
        int p1, p2;
        p1=p2=0;
        if(e1 == null) return e2;
        if(e2 == null) return e1;
        if(e1.ram > e2.ram){
            p1++;
        }else{
            p2++;
        }
        if(e1.cpu > e2.cpu){
            p1++;
        }else{
            p2++;
        }
        if(e1.rtt > e2.rtt){
            p2++;
        }else{
            p1++;
        }
        return p1 > p2 ? e1 : e2;
    }

    public double calculaEstado(){
        return this.cpu;
    }

    @Override
    public int compareTo(Estado o) {
        Float c = new Float(this.calculaEstado());
        return c.compareTo(new Float(o.calculaEstado()));
    }

    @Override
    public int compare(Estado o1, Estado o2) {
        return Double.compare(o1.calculaEstado(), o2.calculaEstado());
    }

    @Override
    public String toString() {
        return "Estado{\n" +
                "\tram=" + ram +
                "\n\tpu=" + cpu +
                "\n\trtt=" + rtt +
                "\n\tadd=" + add +
                "\n\tport=" + port +
                "\n\tbandwidth=" + bandwidth +
                "\n}";
    }
}

public class TabelaDeEstado {
    public Map<InetAddress, Estado> tab;

    public TabelaDeEstado() {
        tab = new ConcurrentHashMap<InetAddress, Estado>();
    }

    public void upBW(InetAddress ip, float bw){
        Estado est = tab.get(ip);
        if (est != null) {
            Estado e = tab.get(ip);
            e.bandwidth = bw;
        }
    }

    public void adicionaEstado(long ram, double cpu, float rtt, InetAddress add, int port){
        Estado est = tab.get(add);
        if (est != null){
            est.cpu = cpu;
            est.ram = ram;
            est.rtt = rtt;
        }
        else{
            est = new Estado(ram, cpu, rtt, add, port);
            tab.put(add,est);
        }
    }

    public InetAddress melhorEstado(){
        Estado opt = null;
        for(Estado e : tab.values()){
            if(opt!=null){
                opt = Estado.melhorEst(e,opt);
            }else{
                opt = e;
            }
        }
        return opt != null ? opt.add : null;
    }


    @Override
    public String toString() {
        String print = null;
        int i = 0;
        for(Estado e : tab.values()){
            print+=String.valueOf(i);
            print+=":";
            print+=e.toString();
            i++;
        }
        return print;
    }
}
