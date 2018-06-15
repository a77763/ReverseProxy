public class ReverseProxy {

    public static void main(String[] args){
        TabelaDeEstado tab = new TabelaDeEstado();
        try {
            Thread udp = new Thread(new MonitorUDP(tab));
            Thread tcp = new Thread(new MonitorTCP(80, tab));

            udp.start();
            tcp.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
