package com.example.notetaker;

import java.util.ArrayList;
import java.util.Random;

public class UniqueAlarmKeyGenerator {
    public static ArrayList<Integer> KeysInUse=new ArrayList<>();
    public static int GetRandomUniqueKey(){
        Random random=new Random();
        Integer UniqueKey=random.nextInt()+1;         /////////This is just to ensure that UniqueKey will never be 0 since we're using 0 as the default for not having any alarm set
        while(KeysInUse.contains(UniqueKey)){       ///////////Repeated random number generator till a unique key is generated
            UniqueKey=random.nextInt()+1;
        }
        KeysInUse.add(UniqueKey);
        return UniqueKey;
    }
    public static void RecycleUniqueKey(int UniqueKey){
        //Need to convert to Integer because remove() can also use index /////////////////////////////////////////////////
        Integer toRemove= UniqueKey;
        if(KeysInUse.contains(toRemove)){
            KeysInUse.remove(toRemove);
        }
    }
}
