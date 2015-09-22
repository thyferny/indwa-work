FILES= *.c
OBJS= *.o
SO=alpine_miner.so
DB=miner_demo
INC=-I$(ORACLE_HOME)/rdbms/public/
DEFINE_FLAG=
#DEFINE_FLAG=-DALPINE_DEBUG
all: $(FILES)
	gcc ${DEFINE_FLAG} -fpic $(INC) -c $(FILES)
	gcc -shared -o $(SO) $(OBJS)
	cp $(SO) $(ORACLE_HOME)/lib/
clean:
	rm $(SO) $(OBJS) 
