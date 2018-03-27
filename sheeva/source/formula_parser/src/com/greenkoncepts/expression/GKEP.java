package com.greenkoncepts.expression;

import org.nfunk.jep.*;

public class GKEP extends JEP{
	ReadingExtract extrFunc;
	public GKEP(){
		super();
		//Add ReadingExtract function
		extrFunc =  new ReadingExtract();
		addFunction("extr", extrFunc);
	}
	
//	public void clearFirtTimeState(){
//		extrFunc.clearFirstState();
//	}

}
