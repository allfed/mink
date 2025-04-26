# MAKE SURE YOU'RE IN THE ROOT DIRECTORY. Then run these, may take about 20 minutes:
read "WAIT, ARE YOU IN THE ROOT DIRECTORY (typically \"mink/\")? IF SO, PRESS ANY KEY. Ensure you have compiled java with ./compile_java in sge_mink3daily and generated the SNX files with the python generate_SNX.py in the SNX_scripts folder."

mv scenarios/world/tests/rice_riCL00IF_150Tg_bug2_* scenarios/world/

./generate_scenarios_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IR.yaml; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IR.yaml DSSAT; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IR.yaml process
./generate_scenarios_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RF.yaml; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RF.yaml DSSAT; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RF.yaml process
./generate_scenarios_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IRRF.yaml; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IRRF.yaml DSSAT; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_IRRF.yaml process
./generate_scenarios_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RFIR.yaml; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RFIR.yaml DSSAT; ./run_from_csv.sh scenarios/world/rice_riCL00IF_150Tg_bug2_RFIR.yaml process

./scenarios/world/tests/test_rf_ir_works_correctly_V2.sh