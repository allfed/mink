
rm(list=ls())
setwd("//home/dmrivers/Code/mink/DSSAT48/Weather/")
library(DSSAT)
library(tidyverse)

###find the executable of DSSAT
options(DSSAT.CSM = "//home/dmrivers/Code/mink/DSSAT48/dssat_compiled_fast.exe")

##################################################################
##add all the soil files in the 7 states into SOIL.SOL,         ##
##all the soil ids are in HN.SOL file, can just read/do it once ##
##################################################################
soil_ids = c('HN_GEN0010', 'HN_GEN0011','HN_GEN0012','HN_GEN0013','HN_GEN0014')

for (id in soil_ids){
  sol = read_sol("../Soil/HN.SOL", id_soil = id)
  write_sol(sol, '../Weather/SOIL.SOL', append = TRUE)
}

##########################################################################
##read each of the weather file in control folder and catastrophe       ##
##folder, and rename it with number, and copy into DSSAT weather folder,##
##so DSSAT can use it.The weather files are names with numbers          ##
###corresponding to the states order in the simulation                  ## 
##########################################################################

main_folders = list.dirs(recursive = F)

for (main_folder in main_folders){
  sub_folders = list.dirs(main_folder, recursive = F)
  for (i in 1:length(sub_folders)){
    wth_file = "/RRRR.WTH"
    if (file.exists(paste0(sub_folders[i], wth_file))){
      if(main_folder=="./wth_control"){
        print("paste0(sub_folders[i], wth_file)")
        print(paste0(sub_folders[i], wth_file))
        print("paste0(sub_folders[i], /RRRR000,i,.WTH)")
        print(paste0(sub_folders[i], "/RRRR000",i,".WTH"))

        file.copy(paste0(sub_folders[i], wth_file), paste0(sub_folders[i], "/RRRR_copy.WTH"))
        file.rename(paste0(sub_folders[i], "/RRRR_copy.WTH"),  paste0(sub_folders[i], "/RRRR000",i,".WTH"))
        file.copy(paste0(sub_folders[i], "/RRRR000",i,".WTH"), "//home/dmrivers/Code/mink/DSSAT48/Weather/", overwrite = TRUE)
      }
      if (main_folder=="./wth_catastrophe_greenhouse"){
        file.copy(paste0(sub_folders[i], wth_file), paste0(sub_folders[i], "/RRRR_copy.WTH"))
        file.rename(paste0(sub_folders[i], "/RRRR_copy.WTH"),  paste0(sub_folders[i], "/RRRR0",i,"0",i,".WTH"))
        file.copy(paste0(sub_folders[i], "/RRRR0",i,"0",i,".WTH"), "//home/dmrivers/Code/mink/DSSAT48/Weather/", overwrite = TRUE)
      }else{
        file.copy(paste0(sub_folders[i], wth_file), paste0(sub_folders[i], "/RRRR_copy.WTH"))
        file.rename(paste0(sub_folders[i], "/RRRR_copy.WTH"),  paste0(sub_folders[i], "/RRRR0",i,i,i,".WTH"))
        file.copy(paste0(sub_folders[i], "/RRRR0",i,i,i,".WTH"), "//home/dmrivers/Code/mink/DSSAT48/Weather/", overwrite = TRUE)
    }
   }
  }
}

#################################################################
###build file_X for each crop in DSSAT crop folder             ##
###and replace with the weather and soil file for each state   ##
###the file is named as LZAF0101.XXX for each crop             ##
#################################################################

  
#######simulation of different crops at different states
### the order of simulation states is MS, CA, ND, IN, KS, ND, WA
###the soil id, wth id, and states are corresponding to each other
states = c('MS', 'CA','MD', 'IN', 'KS', 'WA','ND')
id_sols = c('HN_GEN0013','HN_GEN0014','HN_GEN0013','HN_GEN0013','HN_GEN0012','HN_GEN0011','HN_GEN0010')
crops = c('Maize','Wheat','Soybean','Potato', 'Canola')
crops_s = c('MZ','WH','SB','PT','CN')
ends = c('MZX','WHX','SBX','PTX','CNX')

######set up the *X file for each crop at each state
smry_all = NULL

for (i in 1:length(crops)){
  templates = list.files(paste0("//home/dmrivers/Code/mink/DSSAT48/",crops[i]), pattern = "LZ")
  for (k in templates){
    file_x =  read_filex(paste0("//home/dmrivers/Code/mink/DSSAT48/",crops[i],"/",k))
    for (j in 1:length(states)){
      ###############################################3
      ###for the control files
      file_x$FIELDS$WSTA = paste0("RRRR000",j)  ###change the weather file for the state
      file_x$FIELDS$ID_SOIL = id_sols[j]        ###change the soil file for the state
      print("file_x$FIELDS$WSTA")
      print(file_x$FIELDS$WSTA)

      ###write the new file X
      x = paste0("//home/dmrivers/Code/mink/DSSAT48/",crops[i],"/",substr(k, 3,4),states[j],"0101.", ends[i])
      write_filex(file_x, x)
    
      ####count how many treatment for this crop,as they differ for different crops
      tr = file_x$`TREATMENTS                        -------------FACTOR LEVELS------------`$N  
      tibble(FILEX = x,TRTNO=tr, RP=1, SQ=0, OP=0, CO=0) %>% write_dssbatch()
      print("x")
      print(x)
      run_dssat()        ###run DSSAT
      quit(save="ask")
    
      smry_control = read_output("Summary.out")
      smry_control = smry_control %>% mutate(State = states[j], Crop = crops[i], Scenario ='Control')
      smry_all = rbind(smry_all, smry_control)
    
      ########################################################
      ###for the catastrophe files
      file_x$FIELDS$WSTA = paste0("RRRR0",j,"0",j)  ###change the weather file for the state
      file_x$FIELDS$ID_SOIL = id_sols[j]        ###change the soil file for the state
      ###write the new file X
      x = paste0("//home/dmrivers/Code/mink/DSSAT48/",crops[i],"/",substr(k, 3,4),states[j],"0201.", ends[i])  ##name differently from control
      write_filex(file_x, x)

      ####count how many treatment for this crop,as they differ for different crops
      tr = file_x$`TREATMENTS                        -------------FACTOR LEVELS------------`$N  
      tibble(FILEX = x,TRTNO=tr, RP=1, SQ=0, OP=0, CO=0) %>% write_dssbatch()
      print("x")
      print(x)
      quit(save="ask")
      run_dssat()        ###run DSSAT
      quit(save="ask")
      smry_catastrophe = read_output("Summary.out")
      smry_catastrophe = smry_catastrophe %>% mutate(State = states[j], Crop = crops[i], Scenario ='Catastrophe')
      smry_all = rbind(smry_all, smry_catastrophe)
      
      ####################################################33
      ###for the catastrophe files
      file_x$FIELDS$WSTA = paste0("RRRR0",j,j,j)  ###change the weather file for the state
      file_x$FIELDS$ID_SOIL = id_sols[j]        ###change the soil file for the state
      ###write the new file X
      x = paste0("//home/dmrivers/Code/mink/DSSAT48/",crops[i],"/",substr(k, 3,4),states[j],"0301.", ends[i])  ##name differently from control
      write_filex(file_x, x)
      
      ####count how many treatment for this crop,as they differ for different crops
      tr = file_x$`TREATMENTS                        -------------FACTOR LEVELS------------`$N  
      tibble(FILEX = x,TRTNO=tr, RP=1, SQ=0, OP=0, CO=0) %>% write_dssbatch()
      print(x)
      run_dssat()        ###run DSSAT
      
      smry_greenhouse = read_output("Summary.out")
      smry_greenhouse = smry_greenhouse %>% mutate(State = states[j], Crop = crops[i], Scenario ='Greenhouse')
      smry_all = rbind(smry_all, smry_greenhouse)
    }
  }
}

# # print(EXPER)
# print(smry_all)
# smry_all2 = smry_all %>%  mutate(EXPER = case_when(
#   is.na(TNAM) ~ EXNAME,
#   !is.na(TNAM) ~ paste0(EXNAME, TNAM))) %>% 
#   separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
#   separate(treatment, c('Cultivar','Treatment','Planting'), sep="_") 

# write.csv(smry_all2,'Simulation_All_Crop_Scenario_State_Planting_cleaned.csv',row.names = F)


# smry_cleaned = smry_all2 %>% select(Cultivar, Treatment,Planting,HYEAR, HWAM, State, Crop,Scenario) %>%
#    group_by(Crop, Scenario, State, Cultivar, Treatment, Planting) %>% slice_max(HWAM) %>% ungroup()

# Historical = read_csv('HistoricalObservation.csv') %>% 
#   pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Historical')%>%
#   mutate(Scenario ='Control')

# #############barplot for one year, and no planting difference, discarded, no run##
# ############
# for (c in unique(smry_cleaned$Crop)){
#   tmp = smry_cleaned %>% filter(Crop==c)
#   ggplot(tmp, aes(Scenario, HWAM,fill=Cultivar, group= Cultivar, color=Cultivar)) + 
#     geom_col(position = "dodge") + facet_grid(State~Treatment)+xlab("")+
#     theme(panel.grid.major = element_blank(),
#           panel.grid.minor = element_blank(),
#           panel.background = element_rect(fill="white")) +
#     geom_hline(aes(yintercept = Historical), 
#                data = (Historical %>% filter(Crop==c)), 
#                linetype = "dashed") + 
#     facet_grid(State~Treatment)
#   ggsave(paste0("Simulation_",c,".png"), height = 7)
# }
# ############################################################################

# ############################################################################
# ###new plot with multiple year simulation and different planting dates #####
# ############################################################################
# pdf('Simulations_All_Crops_max.pdf', onefile = TRUE, width = 14, height = 8)
# for (c in unique(smry_cleaned$Crop)){
#     tmp = smry_cleaned %>% filter(Crop==c)  
    
#     tmp$Planting = factor(tmp$Planting, levels = c('P1','P2','P3','P4','P5',
#                                                    'P6','P7','P8','P9','P10'))
    
#     ymax = max(tmp$HWAM) + 2000
    
#     tmp1 = tmp %>% filter(Scenario=='Control')
#     p1 = ggplot(tmp1, aes(Planting, HWAM,fill=Cultivar, group= Cultivar, color=Cultivar)) + 
#       geom_col(position = 'dodge') + facet_grid(State~Treatment)+xlab("")+
#       ylim(0, ymax)+
#       theme(panel.grid.major = element_blank(),
#           panel.grid.minor = element_blank(),
#           panel.background = element_rect(fill="white")) +
#       geom_hline(aes(yintercept = Historical), 
#                data = (Historical %>% filter(Crop==c)), 
#                linetype = "dashed") + 
#       facet_grid(State~Treatment)+ theme(legend.position = "None")+
#       ggtitle(paste0(c,"_Control"))
    
    
#     tmp2 = tmp %>% filter(Scenario=="Catastrophe")
#     p2 = ggplot(tmp2, aes(Planting, HWAM,fill=Cultivar, group= Cultivar, color=Cultivar)) + 
#       geom_col(position = 'dodge') + facet_grid(State~Treatment)+xlab("")+
#       ylim(0, ymax)+
#       theme(panel.grid.major = element_blank(),
#             panel.grid.minor = element_blank(),
#             panel.background = element_rect(fill="white")) +
#      theme(legend.position = "None")+
#       ggtitle(paste0(c,"_Catastrophe"))
    
#    print(ggpubr::ggarrange(p1, p2, ncol=2,common.legend = TRUE, legend="bottom"))
# }

# dev.off()





