package Beans;

abstract class AbstractBean implements Bean {
    protected beanType beanName;
    protected String beanKey;

    public beanType getBeanName() {
        return beanName;
    }

    public void setBeanName(beanType beanName) {
        this.beanName = beanName;
    }

    public String getBeanKey() {
        return beanKey;
    }

    public void setBeanKey(String beanKey) {
        this.beanKey = beanKey;
    }
}
