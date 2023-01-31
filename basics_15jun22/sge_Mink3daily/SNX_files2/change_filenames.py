import os

file_numbers = [1, 3, 5, 7, 9, 11, 13, 15]

for number in file_numbers:
    old_file_name_ir = "sbK{:03d}IR.SNX".format(number)
    old_file_name_rf = "sbK{:03d}RF.SNX".format(number)
    new_file_name_ir = "sbMGROUP" + str(number) + "IR.SNX"
    new_file_name_rf = "sbMGROUP" + str(number) + "RF.SNX"
    os.rename(old_file_name_ir, new_file_name_ir)
    os.rename(old_file_name_rf, new_file_name_rf)
