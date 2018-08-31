package entity;

import java.util.Random;
import java.util.UUID;

public class Account {
    private int fund;
    private String name;
    private String id;

    public Account (String name, int fund){
        this.fund=fund;
        this.name=name;
        this.id= UUID.randomUUID().toString();
    }
    public Account (String name){
        this.fund=new Random().nextInt(10000);
        this.name=name;
        this.id= UUID.randomUUID().toString();
    }

    public Account (){}

    public void setId(String id){
        this.id=id;
    }

    public String getId(){
        return id;
    }

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String toString(){
        String result = "||id: "+this.id+" name: "+this.name+" fund: "+this.fund;
        System.out.println("Account toString call: "+result);
        return result;
    }
}
