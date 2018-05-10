package integration;

import java.lang.management.ManagementFactory;

import com.sun.tools.attach.VirtualMachine;

class AfterVmStartupAgentLoader {

    static void loadAgent(String jarFilePath) {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFilePath, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
