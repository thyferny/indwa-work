override CFLAGS+=-std=gnu99

alpine_miner_lr_ca.o : alpine_miner_lr_ca.c
alpine_miner_array_sum.o : alpine_miner_array_sum.c
alpine_miner_covar.o : alpine_miner_covar.c
alpine_miner_covar_sam.o: alpine_miner_covar_sam.c
alpine_miner_corr.o:alpine_miner_corr.c
alpine_miner_pcaresult.o:alpine_miner_pcaresult.c
alpine_miner_lir_ca.o : alpine_miner_lir_ca.c
alpine_miner_online_sv.o : alpine_miner_online_sv.c
alpine_miner_nn_ca.o : alpine_miner_nn_ca.c
alpine_miner_nb_ca.o : alpine_miner_nb_ca.c
alpine_miner_pr_ca.o : alpine_miner_pr_ca.c
alpine_miner_kmeans_distance.o : alpine_miner_kmeans_distance.c
alpine_miner_plda.o : alpine_miner_plda.c
alpine_miner_em.o : alpine_miner_em.c

MODULE_big = alpine_miner
OBJS = alpine_miner_lr_ca.o alpine_miner_array_sum.o alpine_miner_covar.o alpine_miner_lir_ca.o alpine_miner_online_sv.o alpine_miner_nn_ca.o alpine_miner_nb_ca.o alpine_miner_pr_ca.o  alpine_miner_kmeans_distance.o alpine_miner_covar_sam.o alpine_miner_corr.o alpine_miner_pcaresult.o alpine_miner_plda.o alpine_miner_em.o
PG_CONFIG = pg_config
PGXS := $(shell pg_config --pgxs)
include $(PGXS)
