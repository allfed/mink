
rm(list=ls())
# setwd("E:/ALLFED/all_US_DSSAT-20220917T035443Z-001/all_US_DSSAT/")
setwd("/home/dmrivers/Downloads/wth_control/")

library(DSSAT)
library(tidyverse)
# options(DSSAT.CSM = "C:\\DSSAT48\\DSCSM048.EXE")
options(DSSAT.CSM = "/home/dmrivers/Code/dssat-csm-os/build/bin/dscsm048")

dirs = list.dirs(recursive = F)


##extract id_soil and write into SOIL.SOL
ids = c('HN_GEN0011','HN_GEN0012','HN_GEN0013','HN_GEN0014')

for (id in ids){
  sol = read_sol(paste0(dirs[1],"/HN.SOL"), id_soil = id)
  write_sol(sol, '//home/dmrivers/Downloads/lis_R_code/minkdata/SOIL.SOL', append = TRUE)
}
  
#######simulation of different crops at different states

###the soil id, wth id, and states are corresponding to each other
states = c('MS', 'CA', 'IN', 'KS', 'WA')
id_sols = c('HN_GEN0013','HN_GEN0014','HN_GEN0013','HN_GEN0012','HN_GEN0011')
crops = c('Maize','Wheat','Soybean','Potato', 'Canola')
crops_s = c('MZ','WH','SB','PT','CN')
ends = c('MZX','WHX','SBX','PTX','CNX')

######set up the *X file for each crop at each state
for (i in 1:5){
  file_x =  read_filex(paste0("//home/dmrivers/Downloads/lis_R_code/minkdata/",crops[i],"/LZAF0101.",ends[i]))
  for (j in 1:5){
    file_x$FIELDS$WSTA = paste0("RRRR000",j)
    file_x$FIELDS$ID_SOIL = id_sols[j]
    write_filex(file_x, paste0("//home/dmrivers/Downloads/lis_R_code/minkdata/",crops[i],"/",crops_s[i],states[j],"0101.", ends[i]))
      }
}

########run the dssat simulation
for (i in 1:5){
  smry_states= NULL
  for (j in 1:5){
    x = paste0("//home/dmrivers/Downloads/lis_R_code/minkdata/",crops[i],"/",crops_s[i],states[j],"0101.", ends[i])
    f_x = read_filex(x)
    tr = f_x$`TREATMENTS                        -------------FACTOR LEVELS------------`$N
    
    tibble(FILEX = x,TRTNO=tr, RP=1, SQ=0, OP=0, CO=0) %>% write_dssbatch()
    run_dssat()
    
    smry = read_output("Summary.out")
    
    smry = smry %>% mutate(State = states[j], Crop = crops[i])
    smry_states = rbind(smry_states, smry)
    write.csv(smry_states,paste0(crops[i],'-summary.csv'), row.names = F )
  }
}

############plotting the simulation and compare with observed

Obs = read_csv('HistoricalObservation.csv')

wheat_smry = read_csv('Wheat-summary.csv') %>% select(EXNAME, TNAM, HYEAR, HWAM,State, Crop) %>%
  mutate(EXPER = case_when(
    is.na(TNAM) ~ EXNAME,
    !is.na(TNAM) ~ paste0(EXNAME, TNAM)
  ) ) %>% 
  separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
  separate(treatment, c('Cultivar','Treatment'), sep="_") %>%
  select(-experiment, -TNAM)

obs_wheat = Obs %>% filter(Crop=='Wheat') %>% select(-Total) %>% 
  pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Y_obs')

ggplot(wheat_smry, aes(HYEAR, HWAM, group=Cultivar,color=Cultivar)) + 
  geom_line()+facet_grid(State~Treatment) +
  geom_hline(aes(yintercept = Y_obs), obs_wheat) + facet_grid(State~Treatment)
ggsave('Simulation_Wheat.png')


maize_smry = read_csv('Maize-summary.csv') %>% select(EXNAME,TNAM, HYEAR, HWAM,State, Crop) %>%
  mutate(EXPER = case_when(
    is.na(TNAM) ~ EXNAME,
    !is.na(TNAM) ~ paste0(EXNAME, TNAM)
  ) ) %>% 
  separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
  separate(treatment, c('Cultivar','Treatment'), sep="_") %>%
  select(-experiment, -TNAM)


obs_maize = Obs %>% filter(Crop=='Maize') %>% select(-Total) %>% 
  pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Y_obs')

ggplot(maize_smry, aes(HYEAR, HWAM, group=Cultivar,color=Cultivar)) + 
  geom_line()+facet_grid(State~Treatment) +
  geom_hline(aes(yintercept = Y_obs), obs_maize) + facet_grid(State~Treatment)

ggsave('Simulation_Maize.png')


potato_smry = read_csv('potato-summary.csv') %>% select(EXNAME,TNAM, HYEAR, HWAM,State, Crop) %>%
  mutate(EXPER = case_when(
    is.na(TNAM) ~ EXNAME,
    !is.na(TNAM) ~ paste0(EXNAME, TNAM)
  ) ) %>% 
  separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
  separate(treatment, c('Cultivar','Treatment'), sep="_") %>%
  select(-experiment, -TNAM)


obs_potato = Obs %>% filter(Crop=='Potato') %>% select(-Total) %>% 
  pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Y_obs')

ggplot(potato_smry, aes(HYEAR, HWAM, group=Cultivar,color=Cultivar)) + 
  geom_line()+facet_grid(State~Treatment) +
  geom_hline(aes(yintercept = Y_obs), obs_potato) + facet_grid(State~Treatment)
ggsave('Simulation_Potato.png')

soybean_smry = read_csv('soybean-summary.csv') %>% select(EXNAME,TNAM, HYEAR, HWAM,State, Crop) %>%
  mutate(EXPER = case_when(
    is.na(TNAM) ~ EXNAME,
    !is.na(TNAM) ~ paste0(EXNAME, TNAM)
  ) ) %>% 
  separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
  separate(treatment, c('Cultivar','Treatment'), sep="_") %>%
  select(-experiment, -TNAM)

obs_soybean = Obs %>% filter(Crop=='Soybean') %>% select(-Total) %>% 
  pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Y_obs')

ggplot(soybean_smry, aes(HYEAR, HWAM, group=Cultivar,color=Cultivar)) +
  geom_line()+facet_grid(State~Treatment) +
  geom_hline(aes(yintercept = Y_obs), obs_soybean) + facet_grid(State~Treatment)
ggsave('Simulation_Soybean.png')

canola_smry = read_csv('canola-summary.csv') %>% select(EXNAME,TNAM, HYEAR, HWAM,State, Crop) %>%
  mutate(EXPER = case_when(
    is.na(TNAM) ~ EXNAME,
    !is.na(TNAM) ~ paste0(EXNAME, TNAM)
  ) ) %>% 
  separate(EXPER, c('experiment','treatment'), sep=" ") %>% 
  separate(treatment, c('Cultivar','Treatment'), sep="_") %>%
  select(-experiment, -TNAM)

obs_canola = Obs %>% filter(Crop=='Canola') %>% select(-Total) %>% 
  pivot_longer(c("Rainfed", "Irrigated"), names_to = 'Treatment', values_to = 'Y_obs')

ggplot(canola_smry, aes(HYEAR, HWAM, group=Cultivar,color=Cultivar)) + 
  geom_line()+facet_grid(State~Treatment) +
  geom_hline(aes(yintercept = Y_obs), obs_canola) + facet_grid(State~Treatment)
ggsave('Simulation_Canola.png')
