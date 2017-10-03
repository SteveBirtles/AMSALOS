package Nessy;

import java.util.Random;
import java.util.ArrayList;

public class Gen2{
    //Unnecessary, used to let me visualise the output in python quickly
    //If anyone other than me sees this, it just means I forgot to remove it. Oops.
	/*public static void main(String[]args){
		//Messy code, Rob's spaghetti! :D
		int width=401,height=17;
		int[][]maze=generate(width,height,true);
		System.out.print('[');
		for(int j=0;j<width;j++){
			System.out.print('[');
			for(int i=0;i<height;i++){
				System.out.print(String.valueOf(maze[j][i])+((i<height-1)?",":""));
			}
			System.out.print(']'+((j<width-1)?",":""));
		}
		System.out.println(']');
	}*/

    //Generalised, just in case
    //Border makes the outer-most cells usable if false, else unusable
    public static int[][]generate(int width,int height,boolean border, long seed){
        int[][]cells=new int[width][height];
        ArrayList<double[]>cavernPos=new ArrayList<>();
        int maxx=(border)?1:0,rx=-5,ry;
        Random rand=new Random(seed);

        //Initialise all cells as non-walkable
        for(int i=0;i<width;i++)for(int j=0;j<height;j++)cells[i][j]=128;

        //Select (biased) random coordinates for cavern centers and store coordinates
        for(int x=0;x<width;x++){
            boolean newCavern=(x>maxx)?rand.nextInt(x-maxx)>rx+10:false;
            if(newCavern){
                int y;
                if(border)y=rand.nextInt(height-2)+1;else y=rand.nextInt(height);
                maxx=x;
                cavernPos.add(new double[]{x,y});

                //Adjust dimensions of cavern
                rx=rand.nextInt(5)+3;
                ry=rand.nextInt(4)+2;

                //Add small changes to make the caverns not look identical
                int[][]expansions=new int[4][4];
                for(int i=0;i<4;i++){
                    expansions[i][0]=x+rand.nextInt(5)-2;
                    expansions[i][1]=y+rand.nextInt(5)-2;
                    expansions[i][2]=rand.nextInt(rx)+1;
                    expansions[i][3]=rand.nextInt(ry)+1;
                }

                //Hollow out caverns by setting values to walkable
                for(int a=(border)?1:0;a<width-((border)?1:0);a++){
                    for(int b=(border)?1:0;b<height-((border)?1:0);b++){
                        if(Math.pow(((double)(a-x)/rx),2)+Math.pow(((double)(b-y)/ry),2)<0.9)cells[a][b]=0;
                        for(int[]exp:expansions)if(Math.pow(((double)(a-exp[0])/exp[2]),2)+Math.pow(((double)(b-exp[1])/exp[3]),2)<0.9)cells[a][b]=0;
                    }
                }
            }
        }

        //Bore tunnels between each cavern and the next 2 on the right
        for(int i=0;i<cavernPos.size()-1;i++){
            bore(cells,cavernPos.get(i),cavernPos.get(i+1),border,rand.nextInt(61)-30);
            if(i<cavernPos.size()-2)bore(cells,cavernPos.get(i),cavernPos.get(i+2),border,rand.nextInt(61)-30);
        }

        return cells;
    }

    //Clears out 2 cells to ensure the path is usable
    public static void clear(int[][]cells,double x,double y,boolean border){
        cells[(int)x][(int)y]=0;
        if(x-1>((border)?0:-1))cells[(int)x-1][(int)y]=0;
    }

    public static void bore(int[][]cells,double[]pos,double[]goal,boolean border,int avoid){
        pos=pos.clone();
        int offset=(border)?1:0;
        clear(cells,pos[0],pos[1],border);

        //Continue until the bore's position is 'close enough' to the goal
        while(Math.abs(pos[0]-goal[0])>1||Math.abs(pos[1]-goal[1])>1){
            //Calculate the ideal angle to go straight towards the goal
            double idir=(((goal[0]-pos[0]<0)?3*Math.PI:2*Math.PI)+Math.atan((goal[1]-pos[1])/(goal[0]-pos[0])))%(2*Math.PI);
            if(idir==Double.NaN)idir=0;

            //Steer a certain percentage of π radians away from the ideal angle (up to ±30%)
            //This creates more interesting curved paths which use more of the available space than straight paths
            double dir=idir+Math.PI*avoid/100;

            //Move the bore and ensure it remains in bounds
            pos[0]+=Math.cos(dir)*0.5;
            pos[1]+=Math.sin(dir)*0.5;
            if(pos[0]<offset)pos[0]=offset;
            if(pos[0]>=cells.length-offset)pos[0]=cells.length-1-offset;
            if(pos[1]<offset)pos[1]=offset;
            if(pos[1]>=cells[0].length-offset)pos[1]=cells[0].length-1-offset;

            clear(cells,pos[0],pos[1],border);
        }
    }
}