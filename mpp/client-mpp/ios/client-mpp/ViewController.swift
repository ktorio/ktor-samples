import UIKit

import ios

class ViewController: UIViewController {
    let api = ApplicationApi()

    @IBOutlet weak var aboutText: UITextView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        api.about { (description) -> StdlibUnit in
            self.aboutText.text = description
            return StdlibUnit()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
