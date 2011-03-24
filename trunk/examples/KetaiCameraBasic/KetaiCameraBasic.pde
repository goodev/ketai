import edu.uic.ketai.inputService.KetaiCamera; 
KetaiCamera cam;

void setup() {
  orientation(LANDSCAPE);
  cam = new KetaiCamera(this, 320, 240, 24);
  cam.start();
}

void draw() {
  image(cam, 0, 0, screenWidth, screenHeight);
}

void onCameraPreviewEvent()
{
  cam.read();
}

void exit() {
  cam.stop();
}

void mousePressed()
{
  if (cam.isStarted())
  {
    cam.stop();
  }
  else
    cam.start();
}

