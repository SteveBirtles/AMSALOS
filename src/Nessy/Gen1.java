package Nessy;

import java.util.Random;

public class Gen1 {
    //Unnecessary, used to let me visualise the output in python quickly
    //If anyone other than me sees this, it just means I forgot to remove it. Oops.
    /*public static void main(String[]args){
        int width=401,height=17;
        int[][]maze=generate(width,height);
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

    //Return whether an item is in a list or not
    public static boolean contains(boolean[][]list,boolean item){
        for(boolean sub[]:list)for(boolean t:sub)if(t==item)return true;
        return false;
    }

    //Generalised for mazes of different dimensions, just in case
    public static int[][]generate(int width,int height, long seed){
        int[][]cells=new int[width][height];
        boolean[][]visited=new boolean[width/2][height/2],closed=new boolean[200][8];

        Random rand=new Random(seed);

        //Populate arrays
        for(int x=0;x<width;x++)for(int y=0;y<height;y++)cells[x][y]=(x%2==1&&y%2==1)?0:128;
        for(int x=0;x<width/2;x++)for(int y=0;y<height/2;y++){visited[x][y]=(x==0&&y==0);closed[x][y]=false;}

        int x=0,y=0,dir=0,w=width/2-1,h=height/2-1;

        //Repeat while it hasn't visited every cell
        while(contains(visited,false)){
            //See if there is only one way to go, and go that way if so
            if((x==0||visited[x-1][y])&&(y>0&&!visited[x][y-1])&&(x==w||visited[x+1][y])&&(y==h||visited[x][y+1])){
                cells[x*2+1][y*2]=0;
                y--;
                visited[x][y]=true;
            }else if((x==0||visited[x-1][y])&&(y==0||visited[x][y-1])&&(x<w&&!visited[x+1][y])&&(y==h||visited[x][y+1])){
                x++;
                cells[x*2][y*2+1]=0;
                visited[x][y]=true;
            }else if((x==0||visited[x-1][y])&&(y==0||visited[x][y-1])&&(x==w||visited[x+1][y])&&(y<h&&!visited[x][y+1])){
                y++;
                cells[x*2+1][y*2]=0;
                visited[x][y]=true;
            }else if((x>0&&!visited[x-1][y])&&(y==0||visited[x][y-1])&&(x==w||visited[x+1][y])&&(y==h||visited[x][y+1])){
                cells[x*2][y*2+1]=0;
                x--;
                visited[x][y]=true;
            }else
                //See if it has to backtrack and mark route as closed if so
                if(
                        y>0&&visited[x][y-1]&&!closed[x][y-1]&&cells[x*2+1][y*2]<128&&
                                (x==0||closed[x-1][y]||(visited[x-1][y]&&cells[x*2][y*2+1]>127))&&
                                (x==w||closed[x+1][y]||(visited[x+1][y]&&cells[x*2+2][y*2+1]>127))&&
                                (y==h||closed[x][y+1]||(visited[x][y+1]&&cells[x*2+1][y*2+2]>127))
                        ){
                    closed[x][y]=true;
                    y--;
                }else if(
                        x<w&&visited[x+1][y]&&!closed[x+1][y]&&cells[x*2+2][y*2+1]<128&&
                                (x==0||closed[x-1][y]||(visited[x-1][y]&&cells[x*2][y*2+1]>127))&&
                                (y==0||closed[x][y-1]||(visited[x][y-1]&&cells[x*2+1][y*2]>127))&&
                                (y==h||closed[x][y+1]||(visited[x][y+1]&&cells[x*2+1][y*2+2]>127))
                        ){
                    closed[x][y]=true;
                    x++;
                }else if(
                        y<h&&visited[x][y+1]&&!closed[x][y+1]&&cells[x*2+1][y*2+2]<128&&
                                (x==0||closed[x-1][y]||(visited[x-1][y]&&cells[x*2][y*2+1]>127))&&
                                (x==w||closed[x+1][y]||(visited[x+1][y]&&cells[x*2+2][y*2+1]>127))&&
                                (y==0||closed[x][y-1]||(visited[x][y-1]&&cells[x*2+1][y*2]>127))
                        ){
                    closed[x][y]=true;
                    y++;
                }else if(
                        x>0&&visited[x-1][y]&&!closed[x-1][y]&&cells[x*2][y*2+1]<128&&
                                (x==w||closed[x+1][y]||(visited[x+1][y]&&cells[x*2+2][y*2+1]>127))&&
                                (y==0||closed[x][y-1]||(visited[x][y-1]&&cells[x*2+1][y*2]>127))&&
                                (y==h||closed[x][y+1]||(visited[x][y+1]&&cells[x*2+1][y*2+2]>127))
                        ){
                    closed[x][y]=true;
                    x--;
                }else{
                    //Choose a random, valid, direction to go
                    dir=rand.nextInt(4);
                    if(dir==0&&y>0&&!visited[x][y-1]){
                        cells[x*2+1][y*2]=0;
                        y--;
                        visited[x][y]=true;
                    }else if(dir==1&&x<w&&!visited[x+1][y]){
                        x++;
                        cells[x*2][y*2+1]=0;
                        visited[x][y]=true;
                    }else if(dir==2&&y<h&&!visited[x][y+1]){
                        y++;
                        cells[x*2+1][y*2]=0;
                        visited[x][y]=true;
                    }else if(dir==3&&x>0&&!visited[x-1][y]){
                        cells[x*2][y*2+1]=0;
                        x--;
                        visited[x][y]=true;
                    }
                }
        }

        //Add 25 caverns throughout the maze randomly
        int minx,miny,sx,sy;

        for(int caverns=0;caverns<25;caverns++){
            minx=rand.nextInt(width-2)+1;
            miny=rand.nextInt(height-2)+1;
            sx=rand.nextInt(6)+2;
            sy=rand.nextInt(4)+2;
            while(minx+sx>width-2)sx--;
            while(miny+sy>height-2)sy--;

            for(int i=minx;i<=minx+sx;i++)for(int j=miny;j<=miny+sy;j++)cells[i][j]=0;
        }

        return cells;
    }
}

