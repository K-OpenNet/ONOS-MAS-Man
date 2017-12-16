package Beans;

public interface Bean {
    enum beanType {
        PM, CONTROLLER, SWITCH;
    }

    beanType getBeanName();
    String getBeanKey();
}
