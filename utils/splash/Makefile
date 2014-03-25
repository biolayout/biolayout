FONT=LiberationSans-Bold.ttf
FONT_ITALIC=LiberationSans-BoldItalic.ttf

VERSION_TXT=version.txt

STATIC_SRC= \
	background.png \
	graph.png \
	\
	uoe.png \
	roslin.png \
	embl-ebi.png \
	opengl.png \
	opencl.png

GENERATED_SRC= \
	biolayout.png \
	express.png \
	3d.png \
	credits.png \
	version.png \
	version.txt

SRC= \
	$(STATIC_SRC) \
	$(GENERATED_SRC)

TARGET=splash.png

SHADOW_PARAMS=\( +clone -background black -shadow 80x4+8+8 \) \
    +swap -background none -layers merge +repage

LAST_VERSION=$(shell cat $(VERSION_TXT))
all:
ifneq ($(VERSION),)
ifneq ($(LAST_VERSION),$(VERSION))
	@echo $(VERSION) > $(VERSION_TXT)
endif
endif
	@$(MAKE) $(TARGET) # Do the actual build

biolayout.png:
	convert -background none -fill white \
		-font $(FONT) -pointsize 265 label:'BioLayout' \
		$(SHADOW_PARAMS) $@

express.png:
	convert -background none -fill white \
		-font $(FONT_ITALIC) -pointsize 265 label:'Express' \
		$(SHADOW_PARAMS) $@

3d.png:
	convert -background none -fill white \
		-font $(FONT) -pointsize 165 label:'3D' \
		$(SHADOW_PARAMS) $@

credits.png: credits.txt
	convert -background none -fill white \
		-font $(FONT) -pointsize 46 label:"$(shell cat $<)" \
		$(SHADOW_PARAMS) $@

$(VERSION_TXT):
ifneq ($(VERSION),)
	echo $(VERSION) > $(VERSION_TXT)
else
	echo "undefined" > $(VERSION_TXT)
endif

version.png: $(VERSION_TXT)
	convert -background none -fill white \
		-font $(FONT) -pointsize 46 label:"Version $(shell cat $<)" \
		$(SHADOW_PARAMS) $@

$(TARGET): $(SRC) Makefile
	convert -size 2631x1379 xc:none \
		background.png -geometry +0+0       -composite \
		graph.png      -geometry +0+0       -composite \
		uoe.png        -geometry +13+11     -composite \
		roslin.png     -geometry +264+43    -composite \
		embl-ebi.png   -geometry +58+266    -composite \
		opengl.png     -geometry +1974+1164 -composite \
		opencl.png     -geometry +2394+1146 -composite \
		biolayout.png  -geometry +15+490    -composite \
		express.png    -geometry +15+710    -composite \
		3d.png         -geometry +1070+710  -composite \
		credits.png    -geometry +15+15     -gravity SouthWest -composite \
		version.png    -geometry +10+10     -gravity NorthEast -composite \
		splash.png

clean:
	rm -f $(GENERATED_SRC) $(TARGET)

install: $(TARGET)
	convert -resize 750x393 $(TARGET) ../../src/main/resources/Resources/Images/BioLayoutExpress3DLogo.png

.PHONY: clean all
