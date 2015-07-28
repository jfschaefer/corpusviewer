#!/bin/bash

function change_property {
  sed -i.bak "s/^\($1 *= *\).*$/\1$2/" corpusviewer.properties
}

function switch_to_light_theme {
  change_property stylesheet style.css
  change_property background_color '#ffffff'
}

function switch_to_dark_theme {
  change_property stylesheet dark_style.css
  change_property background_color '#222222'
}

function select_theme {
  echo "Please enter the number of the theme you want"
  select theme in "Light Theme" "Dark Theme"
  do
    if [ "$theme" = "Light Theme" ]; then
      switch_to_light_theme
      exit
    elif [ "$theme" = "Dark Theme" ]; then
      switch_to_dark_theme
      exit
    else
      echo "Invalid theme $theme"
    fi
  done
}

function select_gesture_config {
  echo "Please select the gesture configuration file"
  echo "Remark: Only files matching gesture-config*.properties will be displayed"
  select cf in gesture-config*.properties
  do
    if [ "$cf" = "" ]; then
      echo "Invalid input"
    else
      change_property gesture_config_properties "$cf"
      exit
    fi
  done
}

function set_display_screen {
  echo "Usually, the screen index is 0 (for the primary screen)."
  echo "If you use multiple screens, the index might be different."
  echo "Which screen index do you want to have?"
  read SCREEN_INDEX
  change_property display_screen "$SCREEN_INDEX"
}

function change_fullscreen {
  echo "Do you want to start the program in fullscreen?"
  select answer in "Yes" "No"
  do 
     if [ "$answer" = "Yes" ]; then
       change_property fullscreen true
       exit
     elif [ "$answer" = "No" ]; then
       change_property fullscreen false
       exit
     else
       echo "Invalid input"
    fi
  done
}

function change_trash_zone {
  echo "Do you want the preview zone to be considered trash zone?"
  select answer in Yes No
  do 
     if [ "$answer" = "Yes" ]; then
       change_property preview_is_trash_zone true
       exit
     elif [ "$answer" = "No" ]; then
       change_property preview_is_trash_zone false
       exit
     else
       echo "Invalid input"
    fi
  done
}

echo "Welcome to the configuration tool"
echo "================================="
echo
echo "Warning: This tool hasn't been tested thoroughly yet"
echo
echo "What do you want to do?"
select option in "Select theme" "Change gesture configuration file" "Set display screen" "Change fullscreen" "Edit trash behaviour" "Quit"
do
  if [ "$option" = "Select theme" ]; then
    select_theme
  elif [ "$option" = "Change gesture configuration file" ]; then
    select_gesture_config
  elif [ "$option" = "Set display screen" ]; then
    set_display_screen
  elif [ "$option" = "Change fullscreen" ]; then
    change_fullscreen
  elif [ "$option" = "Edit trash behaviour" ]; then
    change_trash_zone
  elif [ "$option" = "Quit" ]; then
    exit
  else
    echo "Invalid option"
  fi
  echo
  echo "What do you want to do?"
done

