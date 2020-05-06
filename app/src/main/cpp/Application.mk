APP_OPTIM        := release
APP_PLATFORM     := android-27
APP_STL          := system
APP_STL          := gnustl_static
APP_THIN_ARCHIVE := true
APP_PIE:= true


ifneq ($(APP_OPTIM), debug)
  $(info APP_OPTIM is $(APP_OPTIM) ...)
  APP_LDFLAGS  += -Wl,--strip-all
  APP_CFLAGS   += -fvisibility=hidden -fvisibility-inlines-hidden
  APP_CFLAGS   += -g0 -O3 -fomit-frame-pointer -ffunction-sections -fdata-sections
  APP_CPPFLAGS += -std=c++11
endif
