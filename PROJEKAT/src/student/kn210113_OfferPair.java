/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_OfferPair<A, B> implements  PackageOperations.Pair<A,B>{

    private A first;
    private B second;

    public kn210113_OfferPair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    
    @Override
    public A getFirstParam() {
        return first;
    }

    @Override
    public B getSecondParam() {
        return second;
    }

    static boolean equals(PackageOperations.Pair a, PackageOperations.Pair b){
        if(a.getFirstParam().equals(b.getFirstParam()) 
                && a.getSecondParam().equals(b.getSecondParam())){
            return true;
        }
        return false;
    }
    
    
    
}
