import SwiftUI
import shared

class ViewModel : ObservableObject{
    @Published var content: String = "loading"
    
    init() {
        load()
    }

    func load() -> Void {
        ApplicationApi().about { (text) in
            self.content = text
        }
    }
}

struct ContentView: View {
    
    @ObservedObject
    var viewModel = ViewModel()
    
    var body: some View {
        Text(viewModel.content)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
