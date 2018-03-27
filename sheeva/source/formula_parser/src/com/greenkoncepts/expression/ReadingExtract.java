package com.greenkoncepts.expression;

import java.util.ArrayList;

import org.nfunk.jep.*;
import org.nfunk.jep.function.*;

public class ReadingExtract extends PostfixMathCommand implements CallbackEvaluationI{
	java.util.List<DataPoint> storedDatas;
	
	//boolean isNotFirstTime;
	//double storedData;
	int overflow_margin = 100;
	/**
	 * Constructor
	 */
	public ReadingExtract() {
		super();
		numberOfParameters = 1;
		//isNotFirstTime = false;
		//storedData = 0;
		storedDatas = new ArrayList<DataPoint>();
		//System.out.println("ReadingExtract contructing");
	}
	
	public void setOverFlowMargin(int margin){
		overflow_margin = margin;
	}
	
	public int getOverFlowMargin(){
		return overflow_margin;
	}
	
//	void clearFirstState(){
//		isNotFirstTime = true;
//	}
	
	@Override
	public Object evaluate(Node node, EvaluatorI pv) throws ParseException {
		if(node.jjtGetNumChildren()!=1)
			throw new ParseException("Assignment operator must have 1 operator.");

		// evaluate the value of the righthand side.
		//Object rhsVal = pv.eval(node.jjtGetChild(1));	

		// Set the value of the variable on the lhs. 
		Node lhsNode = node.jjtGetChild(0);
		//System.out.print("ReadingExtract: object name=");
		if(lhsNode instanceof ASTVarNode)
		{
			ASTVarNode vn = (ASTVarNode) lhsNode;
			Variable var = vn.getVar();
			String id = var.getName();
			System.out.print(id);
			DataPoint point= new DataPoint(id);;
			int index = storedDatas.indexOf(point);
//			if(index == -1){
//				//point = new DataPoint(id);
//				System.out.print(",initialization with value="+(Double) var.getValue());
//				point.setValue((Double) var.getValue());
//				if(storedDatas.add(point)){
//					System.out.println(",add successfully");
//				}else{
//					System.out.println(",add fail");
//				}
//				var.setValue(0);
//				
//			}else{
//				point = storedDatas.get(index);
//				if(isNotFirstTime){
//					double current = (Double) var.getValue();
//					double previous = point.getValue();
//					double result = 0;
//					if(current >= previous){
//						result = current - previous;
//					}else if(current < previous){
//						if((current < 100) && ((Double.MAX_VALUE-previous)< 100)){
//							result = Double.MAX_VALUE - previous + current;
//						}else{
//							//reset to 0
//							result = 0;
//						}
//					}
//					point.setValue(current);
//					var.setValue(result);
//					System.out.println(",Not first time,current="+current+",previous="+previous+",differental="+result);
//				}else{
//					point.setValue((Double) var.getValue());
//					var.setValue(0);
//					System.out.println(",First time");
//				}
			if(index == -1){
				//point = new DataPoint(id);
				//System.out.print(",initialization with value="+(Double) var.getValue());
				point.setValue((Double) var.getValue());
				if(storedDatas.add(point)){
					//System.out.println(",add successfully");
				}else{
					//System.out.println(",add fail");
				}
				var.setValue(0);
			}else{
				point = storedDatas.get(index);
				double current = (Double) var.getValue();
				double previous = point.getValue();
				double result = 0;
				if(current >= previous){
					result = current - previous;
				}else if(current < previous){
					if((current < 100) && ((Double.MAX_VALUE-previous)< 100)){
						result = Double.MAX_VALUE - previous + current;
					}else{
						//reset to 0
						result = 0;
					}
				}
				point.setValue(current);
				var.setValue(result);
				//System.out.print(",Not first time,current="+current+",previous="+previous+",differental="+result);
				point = storedDatas.set(index, point);
				//System.out.println(".Check Value="+point.getValue());
			}
			//var.setValue(rhsVal);
			return var.getValue();
		}
		throw new ParseException("Node must be an instance of ASTVarNode.");
	}
	
	private class DataPoint{
		double value;
		String id;
		
		public DataPoint(String id){
			this.id = id;
		}
		
		void setValue(double val){
			value = val;
		}
		
		double getValue(){
			return value;
		}
		
		@Override
	    public boolean equals(Object obj) {

	        if(obj instanceof DataPoint){
	           if(((DataPoint) obj).id.equals(id)){
	        	   return true;
	           }
	        }
	        return false;
	    }
	}
	
	/**
	 * Runs the square root operation on the inStack. The parameter is popped
	 * off the <code>inStack</code>, and the square root of it's value is 
	 * pushed back to the top of <code>inStack</code>.
	 */
//	public void run(Stack inStack) throws ParseException {
//
//		// check the stack
//		checkStack(inStack);
//
//		// get the parameter from the stack
//		Object param = inStack.pop();
//
//		// check whether the argument is of the right type
//		if (param instanceof Double) {
//			double result = 0;
//			double currentData = ((Double) param).doubleValue();
//			if(isNotFirstTime){
//				if(currentData >= storedData){
//					result = currentData - storedData;
//				}else if(currentData < storedData){
//					if((currentData < 100) && ((Double.MAX_VALUE-storedData)< 100)){
//						result = Double.MAX_VALUE - storedData + currentData;
//					}else{
//						//reset to 0
//						result = 0;
//					}
//				}
//			}else{
//				storedData = currentData;
//				result = 0;
//			}
//			// push the result on the inStack
//			inStack.push(new Double(result));
//		} else {
//			throw new ParseException("Invalid parameter type");
//		}
//	}



}
